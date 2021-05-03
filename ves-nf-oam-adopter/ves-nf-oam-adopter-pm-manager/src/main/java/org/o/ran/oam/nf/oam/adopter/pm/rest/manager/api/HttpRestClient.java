package org.o.ran.oam.nf.oam.adopter.pm.rest.manager.api;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.time.ZoneId;
import java.util.zip.ZipInputStream;
import org.eclipse.jdt.annotation.NonNull;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.Adapter;

public interface HttpRestClient {
    @NonNull Maybe<ZipInputStream> readFiles(@NonNull Adapter adapter);

    @NonNull Single<ZoneId> getTimeZone(@NonNull Adapter adapter);
}
