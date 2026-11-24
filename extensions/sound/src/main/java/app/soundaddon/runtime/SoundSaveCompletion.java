package app.soundaddon.runtime;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import java.lang.ref.WeakReference;

/** Receives TikTok's native coroutine completion. The patch binds the host Function1 ABI. */
final class SoundSaveCompletion {
    private final WeakReference<Activity> activity;
    private final Object music;
    private final Object viewModel;

    SoundSaveCompletion(Activity activity, Object music, Object viewModel) {
        this.activity = new WeakReference<>(activity);
        this.music = music;
        this.viewModel = viewModel;
    }

    public Object invoke(Object failure) {
        // Clear the temporary ViewModel after its job finishes, on the UI thread.
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                SoundRuntime.saveFinished(activity.get(), failure == null && NativeBridge.isCollected(music));
            } finally {
                NativeBridge.clearSave(viewModel);
            }
        });
        return NativeBridge.completionValue();
    }
}
