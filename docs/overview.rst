.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. SPDX-License-Identifier: CC-BY-4.0
.. Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.

NF OAM Adopter Overview
=======================

NF OAM Adopter provides FM and PM VES event notification capabilities.

Main capabilities are:

- listen for SNMP traps, convert Traps received to VES message format (7.1) and forward it to the VES Collector.
- collect ZIP file of CSV with PM data, translate it to VES message format and forward it to the VES Collector.

Translation is done via configuration file and it is possible to change it at runtime.

Project Resources
-----------------

The source code is available from the Linux Foundation Gerrit server:

`Gerrit <https://gerrit.o-ran-sc.org/r/admin/repos/oam/nf-oam-adopter/>`_

The build (CI) jobs are in the Linux Foundation Jenkins server

`Jenkins <https://jenkins.o-ran-sc.org/view/oam-nf-oam-adopter/>`_

Issues are tracked in the Linux Foundation Jira server:

`Jira <https://jira.o-ran-sc.org/projects/OAM/issues>`_
