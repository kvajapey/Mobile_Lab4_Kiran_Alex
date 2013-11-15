package com.example.trackit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.os.Bundle;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class AppList extends Activity {

	TextView text1;

    ExpandableAppsListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> labels;
    HashMap<String, List<String>> appsByLabel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_list);
		if(!SysBootBroadcastReceiver.isServiceInitiated()){
			Calendar cal = Calendar.getInstance();

			//set AppUsageService as the pending intent for the repeated service
			Intent startServiceIntent = new Intent(this, AppUsageService.class);
			PendingIntent pintent = PendingIntent.getService(this, 0, startServiceIntent, 0);

			AlarmManager alarm = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
			// Start every 0.1 seconds
			alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), ProdUtils.SERVICE_PERIOD, pintent);
		}
		
		text1 = (TextView)findViewById(R.id.productivity_score);
		
        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.expandable_Apps_List);
        
        // preparing list data
        prepareAppsListData();
        
        //initialize adapter
        listAdapter = new ExpandableAppsListAdapter(this, labels, appsByLabel);
        
        // setting list adapter
        expListView.setAdapter(listAdapter);
        
        // Listview Group click listener
        expListView.setOnGroupClickListener(new OnGroupClickListener() {
 
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                    int labelPosition, long id) {
            	/*Toast.makeText(getApplicationContext(),
                "Group Clicked " + labels.get(labelPosition),
                Toast.LENGTH_SHORT).show();*/
                return false;
            }
        });
 
        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new OnGroupExpandListener() {
 
            @Override
            public void onGroupExpand(int labelPosition) {
                Toast.makeText(getApplicationContext(),
                        labels.get(labelPosition) + " Expanded",
                        Toast.LENGTH_SHORT).show();
            }
        });
 
        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {
 
            @Override
            public void onGroupCollapse(int labelPosition) {
                Toast.makeText(getApplicationContext(),
                        labels.get(labelPosition) + " Collapsed",
                        Toast.LENGTH_SHORT).show();
 
            }
        });
 
        // Listview on child click listener
        expListView.setOnChildClickListener(new OnChildClickListener() {
 
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                    int labelPosition, int appPosition, long id) {
            	
            	LayoutInflater layoutInflater 
                = (LayoutInflater)getBaseContext()
                 .getSystemService(LAYOUT_INFLATER_SERVICE);  
               View popupView = layoutInflater.inflate(R.layout.select_label_popup, null);
               
               final PopupWindow popupWindow = new PopupWindow(
                       popupView, LayoutParams.WRAP_CONTENT,  
                             LayoutParams.WRAP_CONTENT);
               
               Button btnProductive = (Button)popupView.findViewById(R.id.set_productive);
               Button btnUnproductive = (Button)popupView.findViewById(R.id.set_unproductive);
               Button btnNoLabel = (Button)popupView.findViewById(R.id.set_nolabel);

               btnProductive.setOnClickListener(new Button.OnClickListener(){

            	   public void onClick(View v) {
            		   // TODO Auto-generated method stub
            		   popupWindow.dismiss();
            	   }});
               
               btnUnproductive.setOnClickListener(new Button.OnClickListener(){

            	   public void onClick(View v) {
            		   // TODO Auto-generated method stub
            		   popupWindow.dismiss();
            	   }});
               
               btnNoLabel.setOnClickListener(new Button.OnClickListener(){

            	   public void onClick(View v) {
            		   // TODO Auto-generated method stub
            		   popupWindow.dismiss();
            	   }});
               
               popupWindow.showAsDropDown(v);
            	
               return false;
            }
        });		
	}

	private void prepareAppsListData() {
		//initiate the lists of labels and apps by label
		labels = new ArrayList<String>();
		appsByLabel = new HashMap<String, List<String>>();
		
		//add different labels
		labels.add("Productive Apps");
		labels.add("Unproductive Apps");
		labels.add("Unlabeled Apps");
		
		//get all the apps from the database to put in lists
		AppsDataSource appData = new AppsDataSource(this);
		appData.open();
		List<AppInfo> allApps;
		
		allApps = appData.getAllApps();
		
		//lists of productive, unproductive, and unlabeled apps
		List<String> productiveApps = new ArrayList<String>();
		List<String> unproductiveApps = new ArrayList<String>();
		List<String> unlabeledApps = new ArrayList<String>();
		
		int i;
		AppInfo currApp;
		String currAppLabel, currAppDisplay;
		//loop to put the apps in designated lists
		for(i = 0; i < allApps.size(); i++){
			currApp = allApps.get(i);
			currAppLabel = currApp.getLabel();
			String formatRunTime = String.format(Locale.getDefault(),"%02d:%02d:%02d", 
					TimeUnit.MILLISECONDS.toHours(currApp.getRunTime()),
					TimeUnit.MILLISECONDS.toMinutes(currApp.getRunTime()) -  
					TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(currApp.getRunTime())), // The change is in this line
					TimeUnit.MILLISECONDS.toSeconds(currApp.getRunTime()) - 
					TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currApp.getRunTime())));  
			currAppDisplay = String.format("%30s %-8s", currApp.getName(), formatRunTime);
			if(currAppLabel == ProdUtils.PRODUCTIVE_LABEL){
				productiveApps.add(currAppDisplay);
			}
			else if(currAppLabel == ProdUtils.UNPRODUCTIVE_LABEL){
				unproductiveApps.add(currAppDisplay);
			}
			else{
				unlabeledApps.add(currAppDisplay);
			}
		}
		
		//put apps into the groups
		appsByLabel.put(labels.get(0), productiveApps);
		appsByLabel.put(labels.get(1), unproductiveApps);
		appsByLabel.put(labels.get(2), unlabeledApps);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_list, menu);
		return true;
	}

}
