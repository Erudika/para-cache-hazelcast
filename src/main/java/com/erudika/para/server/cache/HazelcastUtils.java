/*
 * Copyright 2013-2021 Erudika. https://erudika.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For issues and patches go to: https://github.com/erudika
 */
package com.erudika.para.server.cache;

import com.erudika.para.core.listeners.DestroyListener;
import com.erudika.para.core.utils.Para;
import com.erudika.para.core.utils.Config;
import com.hazelcast.config.AwsConfig;
import com.hazelcast.config.DiscoveryConfig;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;

/**
 * Helper functions for {@link HazelcastCache}.
 *
 * @author Alex Bogdanovski [alex@erudika.com]
 * @see HazelcastCache
 */
public final class HazelcastUtils {

	private static HazelcastInstance hcInstance;

	static {
		// Fix for exceptions from Spring Boot when using a different MongoDB host than localhost.
		System.setProperty("spring.autoconfigure.exclude", String.join(",",
				System.getProperty("spring.autoconfigure.exclude", ""),
				"org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration"));
	}

	private HazelcastUtils() { }

	/**
	 * Initializes a new Hazelcast instance with default settings.
	 * @return a Hazelcast instance
	 */
	public static HazelcastInstance getClient() {
		if (hcInstance == null) {
			hcInstance = Hazelcast.getHazelcastInstanceByName(getNodeName());
			if (hcInstance != null) {
				return hcInstance;
			}
			com.hazelcast.config.Config cfg = new com.hazelcast.config.Config();
			cfg.setInstanceName(getNodeName());
			cfg.setClusterName(Config.CLUSTER_NAME);

			MapConfig mapcfg = new MapConfig("default");
			mapcfg.setEvictionConfig(getEvictionPolicy());
			mapcfg.setTimeToLiveSeconds(Config.getConfigInt("hc.ttl_seconds", 3600));
			mapcfg.setMaxIdleSeconds(mapcfg.getTimeToLiveSeconds() * 2);
	//			mapcfg.setMapStoreConfig(new MapStoreConfig().setEnabled(false).setClassName(NODE_NAME));
			cfg.addMapConfig(mapcfg);
			cfg.setProperty("hazelcast.jmx", Boolean.toString(isJMXOn()));
			cfg.setProperty("hazelcast.logging.type", "slf4j");
			cfg.setProperty("hazelcast.health.monitoring.level", "SILENT");

			if (Config.IN_PRODUCTION && Config.getConfigBoolean("hc.ec2_discovery_enabled", true)) {
				cfg.setProperty("hazelcast.discovery.enabled", "true");
				cfg.setProperty("hazelcast.discovery.public.ip.enabled", "true");

				Map<String, Comparable> awsConfig = new HashMap<>();
				awsConfig.put("access-key", Config.getConfigParam("aws_access_key", System.getenv("AWS_ACCESS_KEY_ID")));
				awsConfig.put("secret-key", Config.getConfigParam("aws_secret_key", System.getenv("AWS_SECRET_ACCESS_KEY")));
				awsConfig.put("region", new DefaultAwsRegionProviderChain().getRegion().id());
				awsConfig.put("host-header", "ec2.amazonaws.com");
				awsConfig.put("security-group-name", Config.getConfigParam("hc.discovery_group", "hazelcast"));

				DiscoveryConfig ec2DiscoveryConfig = new DiscoveryConfig();
				ec2DiscoveryConfig.addDiscoveryStrategyConfig(
						new DiscoveryStrategyConfig("com.hazelcast.aws.AwsDiscoveryStrategy", awsConfig));

				cfg.setNetworkConfig(new NetworkConfig().
						setJoin(new JoinConfig().
							setMulticastConfig(new MulticastConfig().setEnabled(false)).
							setTcpIpConfig(new TcpIpConfig().setEnabled(false)).
							setAwsConfig(new AwsConfig().setEnabled(false)).
							setDiscoveryConfig(ec2DiscoveryConfig)
						)
					);
			}

			hcInstance = Hazelcast.newHazelcastInstance(cfg);

			Para.addDestroyListener(new DestroyListener() {
				public void onDestroy() {
					shutdownClient();
				}
			});
		}

		return hcInstance;
	}

	/**
	 * This method stops the Hazelcast instance if it is running.
	 * <b>There's no need to call this explicitly!</b>
	 */
	protected static void shutdownClient() {
		if (hcInstance != null) {
			hcInstance.shutdown();
			hcInstance = null;
		}
	}

	private static String getNodeName() {
		return Config.PARA.concat("-hc-").concat(Config.WORKER_ID);
	}

	private static EvictionConfig getEvictionPolicy() {
		return new EvictionConfig()
				.setMaxSizePolicy(MaxSizePolicy.PER_NODE)
				.setSize(Config.getConfigInt("hc.max_size", 5000))
				.setEvictionPolicy("LFU".equals(Config.getConfigParam("hc.eviction_policy", "LRU")) ?
						EvictionPolicy.LFU : EvictionPolicy.LRU);
	}

	private static boolean isJMXOn() {
		return Config.getConfigBoolean("hc.jmx_enabled", true);
	}

}
