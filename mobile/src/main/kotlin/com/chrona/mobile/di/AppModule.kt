package com.chrona.mobile.di

import android.content.Context
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun messageClient(@ApplicationContext c: Context): MessageClient = Wearable.getMessageClient(c)
    @Provides @Singleton fun channelClient(@ApplicationContext c: Context): ChannelClient = Wearable.getChannelClient(c)
    @Provides @Singleton fun dataClient(@ApplicationContext c: Context): DataClient = Wearable.getDataClient(c)
    @Provides @Singleton fun nodeClient(@ApplicationContext c: Context): NodeClient = Wearable.getNodeClient(c)
    @Provides @Singleton fun capabilityClient(@ApplicationContext c: Context): CapabilityClient = Wearable.getCapabilityClient(c)
}
