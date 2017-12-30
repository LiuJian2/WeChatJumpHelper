package android.display;

import android.os.IBinder;
import android.view.DisplayInfo;

public interface IDisplayManager {

    DisplayInfo getDisplayInfo(int i);

    abstract class Stub {
        public static IDisplayManager asInterface(IBinder invoke) {
            return null;
        }
    }
}
