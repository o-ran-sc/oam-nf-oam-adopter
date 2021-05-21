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