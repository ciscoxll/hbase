/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.util;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.testclassification.MiscTests;
import org.apache.hadoop.hbase.testclassification.SmallTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({MiscTests.class, SmallTests.class})
public class TestJSONMetricUtil {

  private static final Log LOG = LogFactory.getLog(TestJSONMetricUtil.class);

  @Test
  public void testBuildHashtable() {
    String[] keys = {"type", "name"};
    String[] emptyKey = {};
    String[] values = {"MemoryPool", "Par Eden Space"};
    String[] values2 = {"MemoryPool", "Par Eden Space", "Test"};
    String[] emptyValue = {};
    Map<String, String> properties = JSONMetricUtil.buldKeyValueTable(keys, values);
    assertEquals(values[0], properties.get("type"));
    assertEquals(values[1], properties.get("name"));

    assertNull(JSONMetricUtil.buldKeyValueTable(keys, values2));
    assertNull(JSONMetricUtil.buldKeyValueTable(keys, emptyValue));
    assertNull(JSONMetricUtil.buldKeyValueTable(emptyKey, values2));
    assertNull(JSONMetricUtil.buldKeyValueTable(emptyKey, emptyValue));
  }

  @Test
  public void testSearchJson() throws JsonProcessingException, IOException {
    String jsonString = "{\"test\":[{\"data1\":100,\"data2\":\"hello\",\"data3\": [1 , 2 , 3]}, "
        + "{\"data4\":0}]}";
    JsonNode  node = JSONMetricUtil.mappStringToJsonNode(jsonString);
    JsonNode r1 = JSONMetricUtil.searchJson(node, "data1");
    JsonNode r2 = JSONMetricUtil.searchJson(node, "data2");
    JsonNode r3 = JSONMetricUtil.searchJson(node, "data3");
    JsonNode r4 = JSONMetricUtil.searchJson(node, "data4");
    assertEquals(100, r1.intValue());
    assertEquals("hello", r2.textValue());
    assertEquals(1, r3.get(0).intValue());
    assertEquals(0, r4.intValue());
  }

  @Test
  public void testBuildObjectName() throws MalformedObjectNameException {
    String[] keys = {"type", "name"};
    String[] values = {"MemoryPool", "Par Eden Space"};
    Hashtable<String, String> properties = JSONMetricUtil.buldKeyValueTable(keys, values);
    ObjectName testObject = JSONMetricUtil.buildObjectName(JSONMetricUtil.JAVA_LANG_DOMAIN,
      properties);
    assertEquals(testObject.getDomain(), JSONMetricUtil.JAVA_LANG_DOMAIN);
    assertEquals(testObject.getKeyPropertyList(), properties);
  }

  @Test
  public void testGetLastGCInfo() {
    List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    for(GarbageCollectorMXBean bean:gcBeans) {
      ObjectName on = bean.getObjectName();
      Object value = JSONMetricUtil.getValueFromMBean(on, "LastGcInfo");
      LOG.info("Collector Info: "+ value);
      if (value != null && value instanceof CompositeData) {
        CompositeData cds = (CompositeData)value;
        assertNotNull(cds.get("duration"));
      }
    }
  }
}
