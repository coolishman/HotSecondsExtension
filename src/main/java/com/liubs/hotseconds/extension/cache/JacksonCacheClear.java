package com.liubs.hotseconds.extension.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializerCache;
import com.fasterxml.jackson.databind.ser.SerializerCache;
import com.fasterxml.jackson.databind.ser.impl.ReadOnlyClassToSerializerMap;
import com.liubs.hotseconds.extension.IHotExtHandler;
import com.liubs.hotseconds.extension.holder.InstancesHolder;
import com.liubs.hotseconds.extension.holder.RefreshCoolDown;
import com.liubs.hotseconds.extension.logging.Logger;
import org.hotswap.agent.util.ReflectionHelper;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * @author Liubsyy
 * @date 2024/1/1
 **/
public class JacksonCacheClear implements IHotExtHandler {
    private static Logger logger = Logger.getLogger(JacksonCacheClear.class);


    @Override
    public void afterHandle(ClassLoader classLoader, Class<?> classz, String path, byte[] content) {
        if(null == classz) {
            return;
        }

        Set<ReadOnlyClassToSerializerMap> classToSerializerMaps = InstancesHolder.getInstances(ReadOnlyClassToSerializerMap.class);
        Set<ObjectMapper> objectMappers = InstancesHolder.getInstances(ObjectMapper.class);
        Set<SerializerCache> serializerCaches = InstancesHolder.getInstances(SerializerCache.class);
        Set<DeserializerCache> deserializerCaches = InstancesHolder.getInstances(DeserializerCache.class);

        try{
            if(RefreshCoolDown.INSTANCE.addCoolDown(SerializerCache.class, 3)) {
                classToSerializerMaps.forEach(c->{
                    Object[] _buckets = (Object [])ReflectionHelper.get(c, "_buckets");
                    Arrays.fill(_buckets, null);
                });

                objectMappers.forEach(c->{
                    Map _rootDeserializers = (Map)ReflectionHelper.get(c, "_rootDeserializers");
                    _rootDeserializers.clear();
                });
                serializerCaches.forEach(SerializerCache::flush);

                deserializerCaches.forEach(DeserializerCache::flushCachedDeserializers);
            }
        }catch (Exception e) {
            logger.error("cache clear err",e);
        }


    }
}
