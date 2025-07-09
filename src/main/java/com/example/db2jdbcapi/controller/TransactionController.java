package com.example.db2jdbcapi.controller;

import com.example.db2jdbcapi.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/start")
    public Map<String, String> startTransaction() {
        return transactionService.startTransaction();
    }

    @PostMapping("/query")
    public Object executeQuery(@RequestBody Map<String, String> request) {
        return transactionService.executeQuery(request.get("sessionId"), request.get("sql"));
    }

    @PostMapping("/commit")
    public Map<String, String> commit(@RequestBody Map<String, String> request) {
        return transactionService.commit(request.get("sessionId"));
    }

    @PostMapping("/rollback")
    public Map<String, String> rollback(@RequestBody Map<String, String> request) {
        return transactionService.rollback(request.get("sessionId"));
    }
}
