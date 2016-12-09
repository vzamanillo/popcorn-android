/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.base.manager.provider;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import butter.droid.base.providers.media.MediaProvider;
import butter.droid.base.providers.media.type.MediaProviderType;
import butter.droid.base.providers.subs.SubsProvider;

public class ProviderManager {

    private final List<OnProviderChangeListener> listeners = new ArrayList<>();
    private MediaProviderType currentProviderType;

    private List<MediaProvider> providers = new ArrayList<>();

    public ProviderManager() {
    }

    public List<MediaProvider> getProviders() {
        return providers;
    }

    public MediaProviderType getCurrentMediaProviderType() {
        return currentProviderType;
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    public MediaProvider getCurrentMediaProvider() {
        return getMediaProvider(getCurrentMediaProviderType());
    }

    @MainThread
    public void setCurrentProviderType(MediaProviderType providerType) {
        if (getMediaProvider(providerType) != null) {
            if (this.currentProviderType != providerType) {
                this.currentProviderType = providerType;
                if (listeners.size() > 0) {
                    for (OnProviderChangeListener listener : listeners) {
                        listener.onProviderChanged(providerType);
                    }
                }
            }
        } else {
            throw new IllegalStateException("Provider for type no provided");
        }
    }

    @Nullable
    public MediaProvider getMediaProvider(MediaProviderType providerType) {
        for (MediaProvider provider : providers) {
            if (provider.getProviderType() == providerType) {
                return provider;
            }
        }
        return null;
    }

    public boolean hasProvider(MediaProviderType providerType) {
        return getMediaProvider(providerType) != null;
    }

    public void addProviderListener(@NonNull OnProviderChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeProviderListener(@NonNull OnProviderChangeListener listener) {
        if (listeners.size() > 0) {
            listeners.remove(listener);
        }
    }

    public SubsProvider getCurrentSubsProvider() {
        return getCurrentMediaProvider().getSubsProvider();
    }

    public boolean hasCurrentSubsProvider() {
        return getCurrentMediaProvider().hasSubsProvider();
    }


    public interface OnProviderChangeListener {
        @MainThread
        void onProviderChanged(MediaProviderType mediaProviderType);
    }

}
