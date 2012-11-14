package com.lutshe;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static java.util.Calendar.DECEMBER;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SlidingDrawer;

import com.bugsense.trace.BugSenseHandler;
import com.lutshe.controller.InternetController;
import com.lutshe.controller.MessageDisplayController;
import com.lutshe.controller.RateViewController;
import com.lutshe.controller.UserMessageController;
import com.lutshe.points.PointsController;
import com.lutshe.store.Store;

public class MainActivity extends Activity {

    private static final Random RANDOM = new Random();

	private FinalCountdown timer;

    private MediaPlayer mediaPlayer;
	private NotificationsListAdapter listAdapter;
	private RateViewController rateViewController;
	private UserMessageController userMessageController;
	private PointsController pointsController;
    private Store store;
    
    private MessageDisplayController messagesController;
    private NotificationProvider provider;
    
    public static MainActivity instance;
    
//    private View mainView;
//    
//    private View getMainView(){
//    	LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//        return inflater.inflate(R.layout.main, null);
//    }
    
//    @Override
//    public View findViewById(int id) {
//    	return mainView.findViewById(id);
//    }
    
    @Override
    public void onCreate(Bundle bundle) {
        try {
        	
	    	super.onCreate(bundle);
	    	MainActivity.instance = this;
	    	
	    	BugSenseHandler.initAndStartSession(MainActivity.this, "f48c5119");
	    	
//	        mainView = getMainView();
	      	setContentView(R.layout.main);
	        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
	        store = new Store(getApplicationContext());
	        pointsController = new PointsController(this);

            if (InternetController.isNetworkAvailable(getApplicationContext())) {
	            SynchronizationService.start(getApplicationContext());
            }
	        provider = NotificationProvider.getInstance(getResources(), getApplicationContext());
	        messagesController = new MessageDisplayController(provider, this);
	        messagesController.init();
	
	        setupHistoryList(provider, messagesController);
	        
	        findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	                SendToChooser sendToChooser = new SendToChooser(MainActivity.this, messagesController.getCurrentMessage().getMainText(), pointsController);
	                sendToChooser.sendViaCustomChooser();
	            }
	        });
	        
	        findViewById(R.id.messageIcon).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int lastNotificationNumber = store.getLastNotificationNumber();
					if (lastNotificationNumber > 1) {
						int next = RANDOM.nextInt(lastNotificationNumber) + 1;
						NotificationTemplate notification = provider.getNotification(next);
						messagesController.setCurrentMessage(notification);
					}
				}
			});
	        
	        findViewById(R.id.survivor_btn).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (getRateViewController().isVisible()) {
						getRateViewController().hideRateView();
					} else {
						getRateViewController().showRateView(false);
					}
				}
			});
	        
	        Button.OnClickListener helpListener = new Button.OnClickListener() {
	            public void onClick(View view) {
					messagesController.flipViews();
	            }
	        };
			findViewById(R.id.help_button_container).setOnClickListener(helpListener);
			findViewById(R.id.help_button).setOnClickListener(helpListener);
			
			playSound();
        } catch (Exception e) {
        	BugSenseHandler.sendException(e);
        }
    }

    private boolean isFirstLaunch() {
		return !store.wasLaunchedBefore();
	}

    private boolean isFirstLaunchToday() {
    	Date today = new Date();
    	Date lastLaunch = new Date(store.getLastLaunchTime());
    	return lastLaunch.getDate() != today.getDate() || lastLaunch.getMonth() != today.getMonth() ;
    }
    
	private void setupHistoryList(final NotificationProvider provider, final MessageDisplayController messageController) {
        ListView historyListView = (ListView) findViewById(R.id.content);
		listAdapter = new NotificationsListAdapter(this, provider);
		historyListView.setAdapter(listAdapter);

        final SlidingDrawer slider = (SlidingDrawer) findViewById(R.id.drawer);

        historyListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                int realPosition = listAdapter.flipPosition(position);
                if (realPosition == 0) {
                	messageController.showGreetingView();
                	return;
                }
				NotificationTemplate notification = provider.getNotifications()[realPosition];
                messageController.setCurrentMessage(notification);
                messageController.showMessageView();
                slider.animateClose();
                if (realPosition == 0 && store.wasPointsAddedOnMsgView()){
                    pointsController.addPoints(6);
                    getUserMessageController().showMessage(getString(R.string.review_bonus_text));
                    store.registerPointsAddingOnMsgView();
                }
            }
        });
    }

    public void playSound() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.stopwatch);
        mediaPlayer.setLooping(true);
        
        int maxVolume = 11;
		float log1=(float)(Math.log(maxVolume-2)/Math.log(maxVolume));
        mediaPlayer.setVolume(1 - log1, 1 - log1);
        mediaPlayer.start();
    }

    public void startCountDown() {
        final Calendar finalDate = Calendar.getInstance();
        finalDate.clear();
        finalDate.set(2012, DECEMBER, 21, 0, 0);

        timer = new FinalCountdown(finalDate.getTimeInMillis() - System.currentTimeMillis(), 51, this);
        timer.start();

    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.exit:
//                showMessage("All the same, this does not stop. It is waiting for you.");
//                System.exit(0);
//                return true;
//            case R.id.help:
//                showMessage("Who will help you?");
//                showMessage("No one!");
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    @Override
    protected void onStop() {
    	super.onStop();
    	if (mediaPlayer != null) {
    		mediaPlayer.pause();
    	}
    	stopCountdown();
    }

	private void stopCountdown() {
    	if (timer != null) {
    		timer.cancel();
    		timer = null;
    	}
	}
    
	@Override
    protected void onPause() {
		MainActivity.instance = null;
        super.onPause();
        stopCountdown();
        
        if (mediaPlayer != null) {
        	mediaPlayer.pause();
        }
        
        final SlidingDrawer slider = (SlidingDrawer) findViewById(R.id.drawer);
        slider.close();
        
        BugSenseHandler.flush(MainActivity.this); 
    }

	@Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
        	instance = this;
            int id = extras.getInt(NotificationServiceThatJustWorks.EXTRA_NAME);
            NotificationTemplate notification = provider.getNotification(id);
            pointsController.addPoints(4);
            getUserMessageController().showMessage(getString(R.string.notification_bonus_text));
            messagesController.setCurrentMessage(notification);
            messagesController.showMessageView();
        }
    }
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		if (hasFocus) {
			if (mediaPlayer != null) {
				mediaPlayer.start();
			}
			MainActivity.instance = this;
			
			if (isFirstLaunch()) {
	        	new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						NotificationServiceThatJustWorks.startService(getApplicationContext());
						return null;
					}
				}.execute((Void)null);
				
	        	store.registerFirstLaunch();
	            pointsController.addPoints(10);
	            store.registerPointsAddingOnCreate();
	            store.registerLaunch();
	        } else if (isFirstLaunchToday()) {
	        	pointsController.addPoints(7);
	            getUserMessageController().showMessage(getString(R.string.welcome_back_bonus_text));
	            store.registerLaunch();
	        }
	        
	        startCountDown();
		}
	}
	
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    public void showMessage(String string) {
        getUserMessageController().showMessage(string);
//        final Toast toast = Toast.makeText(getApplicationContext(), string, LENGTH_LONG);
//        toast.setGravity(0, 0, 0);
//        toast.show();
    }

    public Store getStore() {
		return store;
	}
    
    public PointsController getPointsController() {
		return pointsController;
	}

    private NotificationTemplate nextNotification;
    private int notificationId;

	public synchronized void setCurrentNotification(NotificationTemplate template, int id) {
		nextNotification = template;
		notificationId = id;
	}
	
	public synchronized void checkForUpdates() {
		if (nextNotification != null) {
			store.updateLastNotificationNumber(notificationId);
			listAdapter.notifyDataSetChanged();
			
			messagesController.setCurrentMessage(nextNotification);
			messagesController.showMessageView();
			
			pointsController.addPoints(4);
            getUserMessageController().showMessage(getString(R.string.notification_bonus_text));

			nextNotification = null;
			notificationId = -1;
		}
	}

	public RateViewController getRateViewController() {
		if (rateViewController == null) {
			rateViewController = new RateViewController(this, pointsController, store);
		}
		return rateViewController;
	}
	
	public UserMessageController getUserMessageController() {
		if (userMessageController == null) {
			userMessageController = new UserMessageController(this);
		}
		return userMessageController;
	}
	
}

