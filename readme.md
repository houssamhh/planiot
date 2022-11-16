# PlanIoT

This project contains the following directories:

* `queueuingNetworkComposer`: contains the code to generate the JMT queueing network (as an XML file) that represents the QoS Model to evaluate the performance of data flowing between Edge devices and applications. The code takes as input the IoT system specification as a JSON file. Each XML file that is outputed represents one instance of a QoS Model, i.e. one simulation. 

* `setup1-mediumload`: contains the results of the simulations of the baseline QoS Model and optimized QoS Model of a first experimental setup. We consider a medium-loaded PlanIoT broker that accepts publications to 30 topics (and 80 subscriptions) that provide a load of 121.4 Mb/s. 

* `setup2-realtraces`: we evaluate the scalability of PlanIoT by testing it under a realistic smart space setup where we increase the number of subscriptions to saturate the PlanIoT broker. We rely on real traces for the data generated from IoT devices.

* `setup3-emergency`: we consider an emergency case (fire) that occurs in the realistic smart space setup. As a result, we introduce a new application category (with the highest priority) and new subscriptions.

Each directory contains a `readme.md` file.