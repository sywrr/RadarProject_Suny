package com.example.wifitest;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.PatternMatcher;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    private WifiManager mWifiManager ;
    private String SSID;
    private String WifiPassWord;
    private String localIP;

    public void setLocalIP(String localIP) {
        this.localIP = localIP;
    }

    public String getLocalIP() {
        return localIP;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiSetting();
    }
    public void wifiSetting(){
//        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
//        final NetworkSpecifier specifier =
//                new WifiNetworkSpecifier.Builder()
//                        .setSsidPattern(new PatternMatcher("test", PatternMatcher.PATTERN_PREFIX))
//                        .setBssidPattern(MacAddress.fromString("10:03:23:00:00:00"), MacAddress.fromString("ff:ff:ff:00:00:00"))
//                        .build();
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean isopenWifi = mWifiManager.isWifiEnabled();
        mWifiManager.disableNetwork(1);
        if(!isopenWifi){
//            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            this.startActivity(new Intent((Settings.Panel.ACTION_INTERNET_CONNECTIVITY)));
            wifiConnect();
        }else{
            showToastMsg("wifi设置已经打开");
//            wifiConnect();
            getlocalip();

        }
    }
    private void showToastMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    public void wifiConnect() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            final NetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                    .setSsidPattern(new PatternMatcher("iPhone1", PatternMatcher.PATTERN_PREFIX))
                    .setWpa2Passphrase("112233445566")
                    .build();
            final NetworkRequest request =
                    new NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .setNetworkSpecifier(specifier)
                            .build();

            final ConnectivityManager connectivityManager = (ConnectivityManager)
                    getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    // do success processing here..
                  setLocalIP(getlocalip());
                }

                @Override
                public void onUnavailable() {
                    // do failure processing here..
                    System.err.println("test wifiConnect failure");
                }
            };
            connectivityManager.requestNetwork(request, networkCallback);
             // Release the request when done.
//            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

        public void setSSID(String SSID) {
            this.SSID = SSID;
        }

        public String getSSID() {
            return SSID;
        }

        public void setWifiPassWord(String wifiPassWord) {
            WifiPassWord = wifiPassWord;
        }

        public String getWifiPassWord() {
            return WifiPassWord;
    }

    private String getlocalip(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        System.err.println("ip:"+ipAddress);
        if(ipAddress==0)return "未连接wifi";
        return ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."
                +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));
    }
}
