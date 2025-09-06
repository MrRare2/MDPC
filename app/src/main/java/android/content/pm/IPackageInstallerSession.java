package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

import androidx.annotation.Keep;

@Keep
public interface IPackageInstallerSession extends IInterface {
    @Keep
    abstract class Stub extends Binder implements IPackageInstallerSession {
        public static IPackageInstallerSession asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }
    }
}
