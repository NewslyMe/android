package com.guichaguri.trackplayer.activity;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

public class MyCustomReactViewManager extends SimpleViewManager<CustomView> {

    public static final String REACT_CLASS = "MyCustomReactViewManager";


    @Override
    public String getName() {
        return REACT_CLASS;
    }


    @ReactProp(name = "trackInfoData")
    public void setTrackInfo(CustomView view,  @Nullable ReadableMap readableMap) {
        view.setTrackInfo(readableMap);
        view.invalidate();
    }

    @NonNull
    @Override
    protected CustomView createViewInstance(@NonNull ThemedReactContext reactContext) {
        return new CustomView(reactContext);
    }
    

}
