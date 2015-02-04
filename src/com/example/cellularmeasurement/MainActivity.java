package com.example.cellularmeasurement;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;



import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

@SuppressLint("NewApi")
public class MainActivity extends Activity 
{
	protected PowerManager.WakeLock mWakeLock;
	TextView tv;
	ToggleButton tgbutton;
	EditText filenametext;
	Button locationbutton;
	EditText delay;
	int delay_value;
	TelephonyManager tm;
	SignalStrength strength;
	OutputStreamWriter myOutWriter;
	
	static int log_results = 0;
	static int MRIndex = 0;
	
			
    	private LocationManager locationManager;
    	private LocationListener gpsListener; 
    	private double CurrentLocationLong = 0;
    	private double CurrentLocationLat = 0;
    	String resultStr = "", logfilename="";
	
	
    
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	     	final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	     	this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
	     	this.mWakeLock.acquire();
	        
		tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
	    	tm.listen(mListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	    
        	locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        	gpsListener = new myLocationListener();
        	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
	    
		tv = ((TextView)findViewById(R.id.tv));	 
		tv.setTextColor(Color.parseColor("#4FFF8F"));
		
		tgbutton = (ToggleButton) findViewById(R.id.toggleButton1);
		tgbutton.setTextOn("Real-time mode On");
		tgbutton.setTextOff("Static mode On");
				
        	tgbutton.setOnClickListener(new OnClickListener() 
        	{
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				 if (tgbutton.isChecked()) {log_results = 1;}
				 else { log_results = 0;}
			}
		});
        
        	filenametext = (EditText) findViewById(R.id.editText1);
        	locationbutton = (Button) findViewById(R.id.button1);
        	locationbutton.setOnClickListener(new OnClickListener() 
        	{
			@Override
			public void onClick(View v) 
			{
				try 
				{ 
					File logdirectory = new File("/sdcard/CellularMeasurements_Indoors/");
					// have the object build the directory structure, if needed.
					if(!logdirectory.exists())
						logdirectory.mkdirs();
					
					logfilename = "/sdcard/CellularMeasurements_Indoors/"+filenametext.getText().toString()+"_"+System.currentTimeMillis()+".txt";
					File myFile = new File(logfilename);
					myFile.createNewFile();
					FileOutputStream fOut = new FileOutputStream(myFile);
					myOutWriter = new OutputStreamWriter(fOut);
					myOutWriter.append("-: Log file generated Cellular Measurements :-\n");
					myOutWriter.close();
					fOut.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	public void refreshView()
	{
		int i;
		resultStr = "";
		try 
		{
			if(log_results == 0)
			{
				
				Resources res = getResources();
				String[] measurement_reports = res.getStringArray(R.array.string_array_measurement_reports);
				tv.setText(measurement_reports[MRIndex]);
				resultStr = measurement_reports[MRIndex];
				System.out.println(measurement_reports[MRIndex]);
				MRIndex++;
			}
			else if(log_results == 1)
			{
				Writer output = new BufferedWriter(new FileWriter(logfilename, true));
				Date timestamp = new Date();
				@SuppressWarnings("deprecation")
				String time = ""+timestamp.getHours()+"_"+timestamp.getMinutes()+"_"+timestamp.getSeconds();
				if(log_results ==1) 
				{
					output.append(time+"\n");
					resultStr += time+"\n";
				}
				
				String operator =  tm.getNetworkOperatorName();
				String phonetypes[] = {"None", "GSM", "CDMA", "SIP"};
				String phonetype = phonetypes[tm.getPhoneType()];
				CellLocation loc = tm.getCellLocation();
				
				if(log_results ==1)
				{
					output.append(loc+"\n");
					resultStr += loc+"\n";
				}
				tv.setText("Radio Type - "+phonetype+" "+time+"\n");
					
				if(log_results ==1) 
				{
					output.append("Loc: "+CurrentLocationLat+" "+CurrentLocationLong+"\n");
					resultStr += "Loc: "+CurrentLocationLat+" "+CurrentLocationLong+"\n";
				}
					
				if(phonetype.equals("GSM"))
				{
					tv.append("BER: "+strength.getGsmBitErrorRate()+"\n");
					tv.append("dBm: -"+(-113+2*strength.getGsmSignalStrength())+"\n"); 	
					tv.append("ASU: "+strength.getGsmSignalStrength()+"\n\n");
					if(log_results ==1) 
					{
						output.append("GSM RSSI: "+(-113+2*strength.getGsmSignalStrength())+"\n");
						resultStr += "GSM RSSI: "+(-113+2*strength.getGsmSignalStrength())+"\n";
					}
				}
				  
				if(phonetype.equals("CDMA"))
				{
					tv.append("RSCP: "+strength.getCdmaDbm()+"\n");
					if(log_results ==1) 
					{
						output.append("CDMA RSCP:"+strength.getCdmaDbm()+"\n");
						resultStr += "CDMA RSCP:"+strength.getCdmaDbm()+"\n";
					}
					tv.append("CDMA Ec/I0: "+(strength.getCdmaEcio())+"\n"); 	
					if(log_results ==1) 
					{
						output.append("CDMA Ec/I0: "+(strength.getCdmaEcio())+"\n");
						resultStr += "CDMA Ec/I0: "+(strength.getCdmaEcio())+"\n";
					}
				}
			
				tv.append( "Operator   - "+operator+" ("+tm.getLine1Number()+")\n");
				tv.append( "Cellular Location   - "+loc.toString()+"\n\n");
				tv.append( "Geo Location   - "+CurrentLocationLat+" "+CurrentLocationLong+"\n\n");
					
				int net_type =  tm.getNetworkType();
				int rssi, lac, cid;
				    
				if(net_type == TelephonyManager.NETWORK_TYPE_EDGE )
				{
					List<NeighboringCellInfo> cellinfo = tm.getNeighboringCellInfo();
				    
				    	tv.append("Network Type: EDGE\n"); 
					  
					for(i=0; i< cellinfo.size(); i++)
					{
					    rssi = (-113 + 2 * cellinfo.get(i).getRssi());
					    lac = cellinfo.get(i).getLac();
					    cid = cellinfo.get(i).getCid();
					    
					    tv.append("EDGE Cell "+i+"CID: "+cid+" LAC: "+lac+" rssi: "+rssi+"\n");
					    if(log_results ==1)
					    {
					    	output.append("EDGE Cell "+i+"CID: "+cid+" LAC: "+lac+" rssi: "+rssi+"\n");
					    	resultStr += "EDGE Cell "+i+"CID: "+cid+" LAC: "+lac+" rssi: "+rssi+"\n";
					    }
				    	}		
				}
				   
				if(net_type == TelephonyManager.NETWORK_TYPE_CDMA) 
				{
				    List<NeighboringCellInfo> cellinfo = tm.getNeighboringCellInfo();
				    int rssi;
				    
				    tv.append("Network Type: CDMA\n");
				    for(i=0; i< cellinfo.size(); i++)
				    {
				    	rssi = cellinfo.get(i).getRssi();
				    	tv.append("CDMA Neighboring Cell "+i+" "+rssi+	"\n");
				    	if(log_results ==1) 
				    	{
				    		output.append("CDMA Neighboring Cell "+i+" "+rssi+	"\n");
				    		resultStr += "CDMA Neighboring Cell "+i+" "+rssi+	"\n";
				    	}
				     }
				}
				  
				if(net_type == TelephonyManager.NETWORK_TYPE_UMTS) 
				{
					List<NeighboringCellInfo> cellinfo = tm.getNeighboringCellInfo();
					int rssi,pch;
					
					tv.append("Network Type: UMTS\n");
					for(i=0; i< cellinfo.size(); i++)
					{
						rssi = cellinfo.get(i).getRssi();
						pch = cellinfo.get(i).getPsc();
						tv.append("UMTS Neighboring Cell psc "+pch+" "+rssi+	"\n");
						if(log_results ==1) 
						{
							output.append("UMTS Neighboring Cell "+pch+" "+rssi+	"\n");
							resultStr += "UMTS Neighboring Cell "+pch+" "+rssi+	"\n";
						}
					}
				}
				 
				if(net_type == TelephonyManager.NETWORK_TYPE_HSPA) 
				{
					List<NeighboringCellInfo> cellinfo = tm.getNeighboringCellInfo();
					int rssi, pch;
					
					tv.append("Network Type: HSPA\n");
					for(i=0; i< cellinfo.size(); i++)
					{
						rssi = cellinfo.get(i).getRssi();
						pch = cellinfo.get(i).getPsc();
						tv.append("HSPA Neighboring Cell psc "+pch+" "+rssi+	"\n");
						if(log_results ==1)
						{
							output.append("HSPA Neighboring Cell "+pch+" "+rssi+	"\n");
							resultStr += "HSPA Neighboring Cell "+pch+" "+rssi+	"\n";
						}
					}
				}
				    
				if(net_type == TelephonyManager.NETWORK_TYPE_HSPAP) 
				{
					List<NeighboringCellInfo> cellinfo = tm.getNeighboringCellInfo();
					int rssi, pch;
					
					tv.append("Network Type: HSPAP\n");
					for(i=0; i< cellinfo.size(); i++)
					{
						rssi = cellinfo.get(i).getRssi();
						pch = cellinfo.get(i).getPsc();
						tv.append("HSPA+ Neighboring Cell psc "+pch+" "+rssi+	"\n");
						if(log_results ==1)
						{
							output.append("HSPA Neighboring Cell "+pch+" "+rssi+	"\n");
							resultStr += "HSPA Neighboring Cell "+pch+" "+rssi+	"\n";
						}
					}
				}
				    
				if(net_type == TelephonyManager.NETWORK_TYPE_LTE) 
				{
					List<NeighboringCellInfo> cellinfo = tm.getNeighboringCellInfo();
					int rssi;
					
					tv.append("Network Type: LTE\n");
					for(i=0; i< cellinfo.size(); i++)
					{
						rssi = cellinfo.get(i).getRssi();
						tv.append("LTE Neighboring Cell "+i+" "+rssi+	"\n");
						if(log_results ==1) 
						{
							output.append("LTE Neighboring Cell "+i+" "+rssi+	"\n");
							resultStr += "LTE Neighboring Cell "+i+" "+rssi+	"\n";
						}
					}
				}
				    		    
				if(net_type == TelephonyManager.NETWORK_TYPE_UNKNOWN) 
				{ 
					tv.append("Unknown Network Type\n");
				}
				    
				WifiManager wifi;
				List<ScanResult> results;
				wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
				wifi.startScan();
				results = wifi.getScanResults();     
				try 
				{   	
					for(int k = 0; k<results.size(); k++)
					{
						tv.append("WiFi: "+results.get(k).BSSID+" "+results.get(k).SSID+" "+results.get(k).level+" "+results.get(k).frequency+"\n");
						//resultStr += "WiFi: "+results.get(k).BSSID+" "+results.get(k).SSID+" "+results.get(k).level+" "+results.get(k).frequency+"\n";
						if(log_results ==1) 
						{
							 output.append("WiFi: "+results.get(k).BSSID+" "+results.get(k).SSID+" "+results.get(k).level+" "+results.get(k).frequency+"\n");
							 resultStr += "WiFi: "+results.get(k).BSSID+" "+results.get(k).SSID+" "+results.get(k).level+" "+results.get(k).frequency+"\n";
						}
					}
				}
				catch(Exception e)
				{
				    e.printStackTrace();
				}
				output.close();
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	// Listener for signal strength.
	final PhoneStateListener mListener = new PhoneStateListener() 
	{
		@Override
		public void onSignalStrengthsChanged(SignalStrength sStrength)
		{  		    
			strength = sStrength;
			refreshView();
			SendMessage sendMessageTask = new SendMessage();
			sendMessageTask.execute();
		}
	};
	
	class SendMessage extends AsyncTask<Void, Void, Void> 
	{
		@Override
		protected Void doInBackground(Void... params) 
		{
			try 
			{
				Socket s = new Socket("172.31.82.40", 8123); // connect to the server
				DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());
				if(resultStr != "")
				outToServer.writeBytes(resultStr);
				s.close();
			} 
			catch (UnknownHostException e) 
			{
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			return null;
		}
	}

 
	class myLocationListener implements LocationListener 
	{
		@Override
		public void onLocationChanged(Location loc) 
		{    
			CurrentLocationLong = loc.getLongitude();	       
			CurrentLocationLat = loc.getLatitude();
			Log.e("",CurrentLocationLat+" "+CurrentLocationLong);
		}
		
		@Override
		public void onProviderDisabled(String provider) {}
		
		@Override
		public void onProviderEnabled(String provider) {}
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	}
}
