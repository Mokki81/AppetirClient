package com.appetir;

import net.fabricmc.api.ClientModInitializer;

public class AppetirClient implements ClientModInitializer {

public static final String NAME = "Appetir Client";
public static final String VERSION = "1.0";

@Override
public void onInitializeClient() {
System.out.println(NAME + " loaded!");
}
}