package com.dragovorn.dragonlance;

import skadistats.clarity.Clarity;
import skadistats.clarity.model.CombatLogEntry;
import skadistats.clarity.processor.gameevents.OnCombatLogEntry;
import skadistats.clarity.processor.runner.SimpleRunner;
import skadistats.clarity.source.MappedFileSource;
import skadistats.clarity.wire.common.proto.DotaUserMessages;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class DragonLance {

    private Map<String, AtomicInteger> peopleBashed;

    private int heroBashes;
    private int creepBashes;

    DragonLance(String[] args) throws Exception {
        this.heroBashes = 0;
        this.creepBashes = 0;
        this.peopleBashed = new HashMap<>();

        new SimpleRunner(new MappedFileSource(args[0])).runWith(this);

        System.out.println("Slardar Bash stats in a " + format((int) Clarity.infoForFile(args[0]).getPlaybackTime()) + " long game");
        System.out.println("---");
        System.out.println("General Stats:");
        System.out.println("Slardar Bashed Heros " + this.heroBashes + " times.");
        System.out.println("Slardar Bashed Creeps " + this.creepBashes + " times.");
        System.out.println("Slardar Bashed a total of " + (this.heroBashes + this.creepBashes) + " times.");
        System.out.println("---");
        System.out.println("Heros bashed and number of times bashed:");
        for (Map.Entry<String, AtomicInteger> entry : this.peopleBashed.entrySet()) {
            System.out.println(entry.getKey() + " was bashed " + entry.getValue().get() + " times.");
        }
        System.out.println("---");
        System.out.println("Statistical Calculations:");
        System.out.println("% Heroes Bashed (out of total): " + (float) (((double) this.heroBashes / (this.heroBashes + this.creepBashes)) * 100));
        System.out.println("% Creeps Bashed (out of total): " + (float) (((double) this.creepBashes / (this.heroBashes + this.creepBashes))) * 100);
        for (Map.Entry<String, AtomicInteger> entry : this.peopleBashed.entrySet()) {
            System.out.println("% " + entry.getKey() + " was bashed (out of heroes bashed total): " + ((float) ((double) entry.getValue().get() / this.heroBashes) * 100));
        }
    }

    private String format(int time) {
        int minutes = time / 60;

        return minutes + ":" + time % 60;
    }

    private String compileName(String attackerName, boolean isIllusion) {
        return attackerName != null ? attackerName + (isIllusion ? " (illusion)" : "") : "UNKNOWN";
    }

    private String getAttackerNameCompiled(CombatLogEntry entry) {
        return compileName(entry.getAttackerName(), entry.isAttackerIllusion());
    }

    private String getTargetNameCompiled(CombatLogEntry entry) {
        return compileName(entry.getTargetName(), entry.isTargetIllusion());
    }

    @OnCombatLogEntry
    public void onCombatLogEntry(CombatLogEntry entry) {
        if (entry.getType() != DotaUserMessages.DOTA_COMBATLOG_TYPES.DOTA_COMBATLOG_MODIFIER_ADD) {
            return;
        }

        if (entry.isTargetIllusion()) {
            return;
        }

        if (entry.isAttackerIllusion()) {
            return;
        }

        String name = getAttackerNameCompiled(entry);
        String otherName = getTargetNameCompiled(entry);

        if (name.equalsIgnoreCase("npc_dota_hero_slardar") && entry.getInflictorName().equalsIgnoreCase("modifier_bashed")) {
            if (entry.isTargetHero()) {
                AtomicInteger num = this.peopleBashed.getOrDefault(otherName, new AtomicInteger(0));
                num.incrementAndGet();
                this.peopleBashed.put(otherName, num);

                this.heroBashes++;
            } else {
                this.creepBashes++;
            }
        }
    }
}