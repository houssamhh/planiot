(define (problem problem_name) (:domain domain_name)
(:objects 
    printing energymanagement occupancymanagement videosurveillance firedetection intrusiondetection amazonecho smartthings bms topic_all  - Topic
    app1 app2 app3 app4 app5 app6 app7 app8 app9 app10 app11 app12 app13 app14 app15 app16 app17 app18 app19 app20 app21 app22 app23 app24 app25 app26 app27 app28 app29 app30 app31 app32 app33 app34 app35 app36 app37 app38 app39 app40 app41 app42 app43 app44 app45 app46 app47 app48 app49 app50 app_all - Application
    ;;dropping10 dropping20 baseline priorities finiteCapacity  - QosConfig
    ;;shared-bw maxmin-bw topics-bw  - NetworkPolicy
)
(:init
    ;todo: put the initial state's facts and numeric values here
        (baseline topic_all app_all)
        (priority_not_set topic_all app_all)
        
    #init_predicates#
)
(:goal (and
    ;todo: put the goal condition here
        (QoS_achieved topic_all app_all)
        (priority_set topic_all app_all)
))
;un-comment the following line if metric is needed
(:metric minimize ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + ( + (* 1 (latency amazonecho app1 ))
(* 1 (latency smartthings app3 )))
(* 1 (latency smartthings app8 )))
(* 1 (latency videosurveillance app5 )))
(* 1 (latency energymanagement app2 )))
(* 1 (latency energymanagement app7 )))
(* 1 (latency firedetection app10 )))
(* 1 (latency energymanagement app9 )))
(* 1 (latency energymanagement app8 )))
(* 1 (latency intrusiondetection app8 )))
(* 1 (latency videosurveillance app8 )))
(* 1 (latency bms app9 )))
(* 1 (latency intrusiondetection app6 )))
(* 1 (latency occupancymanagement app3 )))
(* 1 (latency printing app5 )))
(* 1 (latency printing app3 )))
(* 1 (latency occupancymanagement app7 )))
(* 1 (latency videosurveillance app4 )))
(* 1 (latency occupancymanagement app5 )))
(* 1 (latency occupancymanagement app4 )))
(* 1 (latency amazonecho app7 )))
(* 1 (latency firedetection app5 )))
(* 1 (latency firedetection app7 )))
(* 1 (latency amazonecho app2 )))
(* 1 (latency firedetection app2 )))
(* 1 (latency videosurveillance app7 )))
(* 1 (latency printing app6 )))
(* 1 (latency firedetection app9 )))
(* 1 (latency occupancymanagement app10 )))
(* 1 (latency printing app1 )))
(* 1 (latency videosurveillance app6 ))))
)
