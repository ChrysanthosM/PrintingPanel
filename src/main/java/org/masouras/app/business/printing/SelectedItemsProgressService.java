package org.masouras.app.business.printing;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SelectedItemsProgressService {
    private final Map<String, AtomicInteger> progress = new ConcurrentHashMap<>();

    public String startJob() {
        String jobId = UUID.randomUUID().toString();
        progress.put(jobId, new AtomicInteger(0));
        return jobId;
    }
    public void endJob(String jobId) {
        progress.remove(jobId);
    }

    public void increment(String jobId, int increment) {
        if (progress.containsKey(jobId)) progress.get(jobId).addAndGet(increment);
    }

    public int getCurrent(String jobId) {
        return progress.getOrDefault(jobId, new AtomicInteger(0)).get();
    }
}
