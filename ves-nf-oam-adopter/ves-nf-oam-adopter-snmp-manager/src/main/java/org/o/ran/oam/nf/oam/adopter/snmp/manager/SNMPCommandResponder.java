package org.o.ran.oam.nf.oam.adopter.snmp.manager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.o.ran.oam.nf.oam.adopter.api.VesEventNotifier;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.api.TimeZoneOffsetService;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.mapper.SnmpMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.smi.UdpAddress;

class SNMPCommandResponder implements CommandResponder {
    private static final Logger LOG = LoggerFactory.getLogger(SNMPCommandResponder.class);
    private final TimeZoneOffsetService timeZoneOffsetService;
    private final SnmpMapper mapper;
    private final VesEventNotifier vesEventNotifier;

    public SNMPCommandResponder(TimeZoneOffsetService timeZoneOffsetService, SnmpMapper mapper,
            VesEventNotifier vesEventNotifier) {
        this.timeZoneOffsetService = timeZoneOffsetService;
        this.mapper = mapper;
        this.vesEventNotifier = vesEventNotifier;
    }

    /**
     * This method will be called whenever a pdu is received on the given port
     * specified in the listen() method.
     */
    @Override
    public synchronized void processPdu(final CommandResponderEvent cmdRespEvent) {
        LOG.info("Received PDU");
        final PDU pdu = cmdRespEvent.getPDU();
        if (pdu == null) {
            LOG.warn("Ignoring PDU.");
            return;
        }

        final UdpAddress address = (UdpAddress) cmdRespEvent.getPeerAddress();
        final ZoneId optZoneId = timeZoneOffsetService.getTimeZone(address.getInetAddress().getHostAddress());
        final String timeZone = Optional.ofNullable(optZoneId)
                                        .map(zoneId -> "UTC" + LocalDateTime.now().atZone(zoneId).getOffset().toString())
                                        .orElse(null);

        mapper.toEvent(address, timeZone, pdu)
                .flatMapCompletable(vesEventNotifier::notifyEvents)
                .doOnSubscribe(result -> LOG.debug("SNMP Trap processing started"))
                .doOnComplete(() -> LOG.debug("SNMP Trap processed successfully"))
                .doOnError(error -> LOG.error("Failed to process SNMP Trap", error))
                .subscribe();
    }
}
