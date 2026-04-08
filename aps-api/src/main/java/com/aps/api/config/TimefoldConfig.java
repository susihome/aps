package com.aps.api.config;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.config.solver.SolverConfig;
import com.aps.solver.model.SchedulePlanningModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class TimefoldConfig {

    @Bean
    public SolverConfig solverConfig() {
        return SolverConfig.createFromXmlResource("solverConfig.xml");
    }

    @Bean
    public SolverManager<SchedulePlanningModel, UUID> solverManager(SolverConfig solverConfig) {
        return SolverManager.create(solverConfig);
    }
}
