
package ai.raondata.blues.rn;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Collections;
import java.util.List;

public class RNBluesPackage implements ReactPackage {
    /**
     * Provides the {@link RNBluesModule} to the {@link com.facebook.react.ReactApplication}.
     *
     * @param reactContext the {@link ReactApplicationContext} provided by the React Native
     *                     application
     * @return array of modules provided by this package
     */
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
      return Collections.singletonList(new RNBluesModule(reactContext));
    }

    // Deprecated from RN 0.47
    public List<Class<? extends JavaScriptModule>> createJSModules() {
      return Collections.emptyList();
    }


    /**
     * There are currently no {@link ViewManager}(s) provided by this module.
     *
     * @param reactContext provided by the React Native application
     * @return empty list of {@link ViewManager}(s)
     */
    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
      return Collections.emptyList();
    }
}