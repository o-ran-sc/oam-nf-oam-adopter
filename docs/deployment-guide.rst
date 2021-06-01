.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. SPDX-License-Identifier: CC-BY-4.0
.. Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.

Deployment Guide
================

Build

.. code-block:: bash

    cd ves-nf-oam-adopter
    mvn clean install -Pdocker

Go to solution folder and execute

.. code-block:: bash

    cd solution
    docker-compose up

**Environment variables**

- **SERVER_PORT:** Application server port. Default 443.
- **USERNAME:** basic auth username for adapter service. Default admin.
- **PASSWORD:** basic auth password for adapter service. Default admin.

- **KEYSTORE_PATH:** path to ssl certificates key store.
- **KEY_STORE_PASSWORD:** key store password.
- **KEY_PASSWORD:** key password.
- **TRUST_STORE_PATH:** path to ssl certificates trust store.
- **TRUST_STORE_PASSWORD:** trust store password.

- **VES_COLLECTOR:** ves collector url.
- **VES_ENCODED_AUTH:** Base64-encoded basic auth e.g. YWRtaW46YWRtaW4= (admin:admin)

- **CONNECTION_TIMEOUT:** HTTP client connection timeout(seconds). Default value 600.
- **RESPONSE_TIMEOUT:** HTTP client response timeout(seconds). Default value 600.

- **PM_SYNC_TIME_START:** Defines the time for first execution of pull of PM files and forwarding as VES Message.
- **PM_SYNC_TIME_FREQ:** Define the frequency to trigger the pull and forward of PM data after first execution.
- **PM_MAPPING_FILE_PATH:** Defines the path where mapping configuration file is located. Default value.

- **SNMP_PORT:** UDP port to listen SNMP traps. Default value 162.
- **FM_MAPPING_FILE_PATH:** Defines the path where mapping configuration file is located. Default value.



