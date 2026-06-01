package com.deskflow.module.assignment.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ManualAssignRequest(@NotNull UUID agentId) {}