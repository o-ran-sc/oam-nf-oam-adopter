.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. SPDX-License-Identifier: CC-BY-4.0
.. Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.

================
SNMP2VES Manager
================

SNMP Manager library follows `VES Event Listener 7.1 <https://docs.onap.org/projects/onap-vnfrqts-requirements/en/latest/Chapter8/ves7_1spec.html#>`_

FM VES Message
--------------

**Datatype: commonEventHeader**

.. list-table::
   :header-rows: 1

   * - Field
     - Mandatory
     - Default value
     - Configurable
     - Runtime
     - Notes
   * - domain
     - yes
     - fault
     - no
     - no
     - none
   * - eventId
     - yes
     - SNMP Trap RequestID
     - yes
     - yes
     - Uses default defined value if TRAP OID not configured
   * - eventName
     - yes
     - SNMP Fault
     - yes
     - yes
     - Uses default defined value if TRAP OID not configured
   * - priority
     - yes
     - HIGH
     - no
     - no
     - none
   * - reportingEntityName
     - yes
     - NF-OAM-ADOPTER
     - yes
     - yes
     - set value if provided by notification trap and oid-reporting-entity-name configured.
   * - startEpochMicrosec
     - yes
     - System time at the moment of notification generation
     - yes
     - yes
     - set value if provided by notification trap and oid-start-epoch-microsec configured.
   * - lastEpochMicrosec
     - yes
     - System time at the moment of notification generation
     - yes
     - yes
     - set value if provided by notification trap and oid-last-epoch-microsec configured.
   * - sequence
     - yes
     - 0
     - yes
     - yes
     - set value if provided by notification trap and oid-event-sequence configured.
   * - sourceName
     - yes
     - Host Address of SNMP Agent sending the trap notification
     - yes
     - yes
     - set value if provided by notification trap and oid-source-name configured.
   * - version
     - yes
     - 4.1
     - yes
     - no
     - Library only enforces mandatory fields for 4.1 version
   * - vesEventListenerVersion
     - yes
     - 7.1
     - none
     - no
     - Library only enforces mandatory fields for 7.1 version
   * - eventType
     - no
     - none
     - no
     - no
     - none
   * - nfcNamingCode
     - no
     - none
     - no
     - no
     - none
   * - nfNamingCode
     - no
     - none
     - no
     - no
     - none
   * - nfVendorName
     - no
     - NF-OAM-ADOPTER-VENDOR
     - yes
     - no
     - none
   * - reportingEntityId
     - no
     - none
     - no
     - yes
     - set value if provided by notification trap and oid-reporting-entity-id configured.
   * - internalHeader Fields
     - no
     - none
     - no
     - no
     - none
   * - sourceId
     - no
     - none
     - none
     - no
     - none
   * - timeZoneOffset
     - no
     - none
     - no
     - no
     - none

**Datatype: faultFields**

.. list-table::
   :header-rows: 1

   * - Field
     - Mandatory
     - Default value
     - Configurable
     - Runtime
     - Notes
   * - alarmCondition
     - yes
     - SNMP Fault
     - yes
     - yes
     - set value if provided by notification trap and trap configured.
   * - eventSeverity
     - yes
     - CRITICAL
     - yes
     - yes
     - set value if provided by notification trap and event-severity configured.
   * - eventSourceType
     - yes
     - SNMP Agent
     - yes
     - no
     - none
   * - faultFieldsVersion
     - yes
     - 4.0
     - no
     - no
     - none
   * - specificProblem
     - yes
     - SNMP Fault
     - no
     - yes
     - set value if provided by notification trap and oid-specific-problem-desc configured.
   * - vfStatus
     - yes
     - ACTIVE
     - no
     - no
     - none
   * - eventCategory
     - no
     - none
     - no
     - yes
     - set value if provided by notification trap and event-category configured.
   * - alarmAdditional Information
     - no
     - no
     - no
     - yes
     - Map of OID with values
   * - alarmInterfaceA
     - no
     - none
     - no
     - yes
     - set value if provided by notification trap and oid-alarm-interface-name configured.

Mapping Configuration
---------------------

Configuration file **fm-ves-message-mapping.yaml** contains all definitions required to define the mapping
from trap provided information to VES Message format

**trap mapping example**

.. literalinclude:: fm-ves-message-mapping.yaml
  :language: YAML

- **global**

     - **reporting-entity-name** Reporting entity name assigned to the event
     - **reporting-entity-id** Reporting entity id assigned to the event
     - **nf-vendor-name** Vendor name assigned to the event

- **traps**
    - **oid** *OID* trap identifier
    - **name** Name of the trap
    - **event-severity** Severity assigned to the event
    - **event-category**  Category assigned to the event
    - **event-source-type** Source type assigned to the event
    - **oid-event-id** *OID* containing the event entity id e.g. port interface
    - **oid-event-sequence** Event sequence 0 on a raise and 1 on a clear
    - **oid-reporting-entity-id** *OID* containing the reporting entity id
    - **oid-source-name** *OID* containing the source name
    - **oid-specific-problem-desc** *OID* containing the trap problem description
    - **oid-start-epoch-microsec** *OID* containing the alarm start epoch
    - **oid-last-epoch-microsec** *OID* containing the alarm last epoch
    - **oid-alarm-interface-name** *OID* containing the interface name

**Output example for port down trap**

.. literalinclude:: PortDOWN.json
  :language: JSON

**Output example for any undefined trap**

Undefined trap will use the mapping defined for default.

.. literalinclude:: unknown-trap.json
  :language: JSON