# Setup3: Emergency Scenario

The first directory named `aiplanning` contains:

* `domain_middleware_template.pddl` and `problem_middleware_template.pddl`: the domain and problem file templates that are instantiated.

* `domain_middleware_generated.pddl` and `problem_middleware_generated.pddl`: the domain and problem file instances that are dynamically generated.

* `ResponseTimes.csv`: the dataset containing the response time values for all data flows under different configurations. We aggregate the results of multiple configurations (`dataset.csv` files); the code for performing the aggregation is not included in this directory.

* `validation`: a directory that contains the results of the simulations that represents the configuration proposed by the AI planner.

We use the metricFF planner to generate the plan using as input the domain and problem file instances. The code for generating the problem and file instances is not part of this directory.

The other directories contain the results of the performance of the first setup used under different configurations. All directories have the following files:

* `dataset.csv`: contains the response time, throughput, and drop rate values per each flow.

* `jmtResults.csv`: contains the output of the JMT simulations as a CSV file.

* `qos.csv`: indicates whether the response time QoS requirements are met or not for each flow.


Each subdirectory represents a certain QoS Model:

* `baseline`: contains the results of the performance of data flows under the baseline configurations.

* `dropping`: contains the results of the performance of data flows under different drop rates (for loss tolerant applications)

* `priorities`: contains the results of the performance of data flows when assigning priorities to different application categories.

The directory contains the following files:

* `app_categories.csv`: the application category for each application.

* `app_qos.csv`: the QoS requirements for each application. 