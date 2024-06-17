package com.gitlab.smarthomepartner.nsdzeroconf;

import java.util.List;
import java.util.concurrent.Executor;
import java.net.InetAddress;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.util.Log;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

public class NSDZeroConf extends CordovaPlugin {

    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;
    private static final String LOG_TAG = "NSDZeroconf";

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.i(LOG_TAG, "initializing...");
        nsdManager = (NsdManager) cordova.getActivity().getSystemService(Context.NSD_SERVICE);
    }
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("getHostname".equals(action)) {
            this.getHostname(callbackContext);
            return true;
        } else if ("register".equals(action)) {
            this.register(callbackContext);
            return true;
        } else if ("unregister".equals(action)) {
            this.unregister(callbackContext);
            return true;
        } else if ("stop".equals(action)) {
            this.stop(callbackContext);
            return true;
        } else if ("watch".equals(action)) {
            String serviceType = args.getString(0); 
            this.watch(serviceType, callbackContext);
            return true;
        } else if ("unwatch".equals(action)) {
            this.unwatch(callbackContext);
            return true;
        } else if ("close".equals(action)) {
            this.close(callbackContext);
            return true;
        } else if ("reInit".equals(action)) {
            this.reinitializeServices(callbackContext);
            return true;
        }
        return false;
    }

    private void getHostname(CallbackContext callbackContext) {
        // TODO
        callbackContext.success("hostname");  
    }

    private void register(CallbackContext callbackContext) {
        // TODO
    }

    private void unregister(CallbackContext callbackContext) {
        // TODO
    }

    private void stop(CallbackContext callbackContext) {
        if (discoveryListener != null) {
            Log.i(LOG_TAG, "Stopping service discovery");
            nsdManager.stopServiceDiscovery(discoveryListener);
            discoveryListener = null;
            callbackContext.success("Stopped watching for services");
        } else {
            callbackContext.error("Not currently watching for services");
        }
    }

    private void watch(final String serviceType, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                discoveryListener = new NsdManager.DiscoveryListener() {
                    @Override 
                    public void onDiscoveryStarted(String regType) {
                        Log.i(LOG_TAG, "Discovery started for: " + regType);
                    }

                    @Override
                    public void onServiceFound(NsdServiceInfo service) {
                        if (serviceType.equals(service.getServiceType())) {
                            nsdManager.resolveService(service, new NsdManager.ResolveListener() {
                                @Override
                                public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                                    callbackContext.error("Failed to resolve service with error code: " + errorCode);
                                }

                                @Override
                                public void onServiceResolved(NsdServiceInfo serviceInfo) {
                                    try {
                                        InetAddress address = serviceInfo.getHost();
                                        if (address != null) {
                                            String ipAddress = address.getHostAddress();
                                            callbackContext.success(ipAddress);
                                        } else {
                                            callbackContext.error("No IP address found for service");
                                        }
                                    } catch (Exception e) {
                                        callbackContext.error("Error retrieving IP address: " + e.getMessage());
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onServiceLost(NsdServiceInfo serviceInfo) {
                        Log.i(LOG_TAG, "Service lost: " + serviceInfo.getServiceName());
                    }

                    @Override
                    public void onDiscoveryStopped(String serviceType) {
                        Log.i(LOG_TAG, "Discovery stopped: " + serviceType);
                    }

                    @Override
                    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                        Log.e(LOG_TAG, "Discovery failed: Error code:" + errorCode);
                        nsdManager.stopServiceDiscovery(this);
                    }

                    @Override
                    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                        Log.e(LOG_TAG, "Discovery failed: Error code:" + errorCode);
                        nsdManager.stopServiceDiscovery(this);
                    }
                };

                nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
            }
        });
    }




    private void unwatch(CallbackContext callbackContext) {
        this.stop(callbackContext);
    }

    private void close(CallbackContext callbackContext) {
        // TODO
    }

    private void reinitializeServices(CallbackContext callbackContext) {
        // TODO
    }
}
