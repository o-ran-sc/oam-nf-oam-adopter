.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. SPDX-License-Identifier: CC-BY-4.0
.. Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.

PM REST Manager
###############

PM REST Manager library follows `VES Event Listener 7.1 <https://docs.onap.org/projects/onap-vnfrqts-requirements/en/latest/Chapter8/ves7_1spec.html#>`_

.. toctree::
   :maxdepth: 3
   :caption: Contents:

PM VES Message
==============

Datatype: commonEventHeader
~~~~~~~~~~~~~~~~~~~~~~~~~~~

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
     - measurement
     - no
     - no
     - none
   * - eventId
     - yes
     - none
     - yes
     - yes
     - Unique id generated from combination of multiple fields, selection of fields configurable via mapping config file.
   * - eventName
     - yes
     - PM Notification
     - yes
     - yes
     - value configurable via mapping config file.
   * - priority
     - yes
     - HIGH
     - yes
     - no
     - value configurable via mapping config file.
   * - reportingEntityName
     - yes
     - NF-OAM-ADOPTER
     - yes
     - yes
     - value configurable via mapping config file.
   * - startEpochMicrosec
     - yes
     - none
     - none
     - yes
     - System time at the moment of notification generation
   * - lastEpochMicrosec
     - yes
     - none
     - none
     - yes
     - System time at the moment of notification generation
   * - sequence
     - yes
     - none
     - no
     - yes
     - incremental per line on csv file. Each file will start processing will start from sequence 0.
   * - sourceName
     - yes
     - none
     - yes
     - yes
     - value configurable via mapping config file.
   * - version
     - yes
     - 4.1
     - no
     - no
     - Library only enforces mandatory fields for 4.1 version
   * - vesEventListenerVersion
     - yes
     - 7.1
     - no
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
     - ISCO
     - yes
     - no
     - value configurable via mapping config file.
   * - reportingEntityId
     - no
     - ONAP-ISCO-ADAPTER
     - yes
     - no
     - value configurable via mapping config file.
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


Datatype: measurementFields
~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. list-table::
   :header-rows: 1

   * - Field
     - Mandatory
     - Default value
     - Configurable
     - Runtime
     - Notes
   * - additionalFields
     - no
     - none
     - yes
     - yes
     - values configurable via mapping config file.
   * - additionalMeasurements
     - yes
     - none
     - yes
     - yes
     - values configurable via mapping config file.
   * - measurementInterval
     - yes
     - none
     - yes
     - no
     - value configurable via mapping config file.
   * - measurementFieldsVersion
     - yes
     - 4.0
     - no
     - no
     - none

REST PM Configuration
=====================

Rest Adapter configuration
~~~~~~~~~~~~~~~~~~~~~~~~~~

Configuration file **application.yml** contains global definitions required to be consumed by PM REST adapters services.

- **token-api-username** Defines the username to be used for login
- **synchronization-time-start** Defines the time for execution of pull of PM files and forwarding as VES Message
- **synchronization-time-frequency** Defines the time for execution of pull of PM files and forwarding as VES Message
- **mapping-config-path** Defines the path where mapping configuration file is located

Rest SB Client Adapter configuration
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

- **ran-token-endpoint** Defines rand endpoint from where token will be obtained
- **ran-pm-endpoint:** Defines rand endpoint from where ZIP with PM files will be GET.
- **ran-time-zone-offset-endpoint** Defines rand endpoint from where we GET the timezone from the device.

.. literalinclude:: pm-rest-manager.yaml
  :language: YAML

Mapping Configuration
~~~~~~~~~~~~~~~~~~~~~

Configuration file **pm-ves-message-mapping.yaml** contains all definitions required to define the mapping
from ZIP file with collection of csv files information to VES Message format

.. note::
   File name can be changed as long it matches with configured **mapping-config-path** and file is in yaml format

- **reporting-entity-name** Reporting entity name assigned to the event
- **reporting-entity-id** Reporting entity id assigned to the event
- **nf-vendor-name** Vendor name assigned to the event
- **event-source-type** Source type assigned to the event
- **event-name** Event name assigned to the event
- **measurement-interval** Interval over which measurements are being reported in seconds
- **priority** Priority assigned to the event

- **CSV**
    - **source-name** Column names containing containing information
    - **source-name-regex** The regular expression to which source-name string value is to be matched and removed
    - **event-id** Collection of columns names to generate an uniqueID
    - **additional-fields** Collection of columns names containing information to be attached
    - **additional-measurements-name** Name assigned to AdditionalMeasurement array
    - **additional-measurements** Collection of columns names containing information to be attached
    - **batch-size** File will be processed and send in small batches event notifications. Size define the number of events which will contain each notification

.. literalinclude:: pm-ves-message-mapping.yaml
  :language: YAML

PM Adapter Configuration
========================

PM Adapters can be instantiated at runtime

Create a PM Adapter
~~~~~~~~~~~~~~~~~~~

**POST** ``https://<SERVICE_IP>:<SERVICE_PORT>/adapters/adapter``

.. literalinclude:: create-adapter.json
  :language: JSON

Get PM Adapters
~~~~~~~~~~~~~~~

**GET** ``https://<SERVICE_IP>:<SERVICE_PORT>/adapters/``

.. literalinclude:: get-adapters.json
  :language: JSON


Delete PM Adapter
~~~~~~~~~~~~~~~~~

**DELETE** ``https://<SERVICE_IP>:<SERVICE_PORT>/adapters/adapter/10.53.40.50``
