package config;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import config.DirectMappedPageTableConfig;
import config.InverseMappedPageTableCfg;

public class ConfigReader {

	/**
	 * also closes the input stream
	 * 
	 * @param is
	 * @return
	 */
	public static void setConfig(InputStream is) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		;
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();

			Element rootNode = builder.parse(is).getDocumentElement();
			String value;
			ConfigHolder.generalCfg.setVirtualAddrNBits(getElementIntValue(
					rootNode, "virtualAddressNBits", 32));
			ConfigHolder.generalCfg.setNumberProcessesNBits(getElementIntValue(
					rootNode, "numberProcessesNBits", 0));
			ConfigHolder.generalCfg.setDiskAccessTime(getElementIntValue(
					rootNode, "diskATU", 1));
			// page table configuration
			NodeList nl = rootNode.getElementsByTagName("pageTable");
			Element elemPt;
			if (nl != null && nl.getLength() > 0) {
				elemPt = (Element) nl.item(0);
				if (elemPt.getAttribute("direct") != null
						&& Boolean.parseBoolean(elemPt.getAttribute("direct"))) {
					ConfigHolder.pageTableCfg.setDirectMapped(true);
					DirectMappedPageTableConfig dptcfg = (DirectMappedPageTableConfig) ConfigHolder.pageTableCfg
							.getAddCfg();
					// TODO check offsetsLength to exist
					if (elemPt.getElementsByTagName("offsetLengths") != null
							&& elemPt.getElementsByTagName("offsetLengths")
									.getLength() > 0) {
						NodeList offsets = ((Element) elemPt
								.getElementsByTagName("offsetLengths").item(0))
								.getElementsByTagName("length");
						if (offsets != null && offsets.getLength() > 0) {
							int[] offsetLengths = new int[offsets.getLength()];
							for (int i = 0; i < offsets.getLength(); i++) {
								value = ((Text) ((Element) offsets.item(i))
										.getFirstChild()).getNodeValue();
								try {
									offsetLengths[i] = Integer.parseInt(value);
								} catch (NumberFormatException e) {
									offsetLengths[i] = 0;
								}
							}
							dptcfg.setOffsetsLength(offsetLengths);
						}
					}
					if (elemPt.getElementsByTagName("searchMethod") != null
							&& elemPt.getElementsByTagName("searchMethod")
									.getLength() > 0) {
						value = ((Text) ((Element) elemPt.getElementsByTagName(
								"searchMethod").item(0)).getFirstChild())
								.getNodeValue();
						dptcfg.setSearchMethodTopDown(value != null
								&& value.equalsIgnoreCase("topdown"));
					}
				} else {
					ConfigHolder.pageTableCfg.setDirectMapped(false);
					InverseMappedPageTableCfg icfg = (InverseMappedPageTableCfg) ConfigHolder.pageTableCfg
							.getAddCfg();
					icfg.setHashAnchorSizeNBits(getElementIntValue(rootNode,
							"hashAnchorSizeNBits", 0));
				}
				NodeList nlTlb = elemPt.getElementsByTagName("tlbConfig");
				if (nlTlb != null && nlTlb.getLength() > 0) {
					Element tlbElem = (Element) nlTlb.item(0);
					setCacheConfig(ConfigHolder.pageTableCfg.getTlbConfig(),
							tlbElem);

				}
			}
			
			nl = rootNode.getElementsByTagName("pageAgingConfig");
			if(nl!=null && nl.getLength()>0){
				ConfigHolder.generalCfg.pageAgingConfig.setPageAgingIncrease(getElementIntValue((Element)nl.item(0) , "pageAgingIncrease" , -1));
				ConfigHolder.generalCfg.pageAgingConfig.setMemRefToBeRun(getElementIntValue((Element)nl.item(0) , "memRefToRun" , -1));
			}
			
			nl = rootNode.getElementsByTagName("memAllocConfig");
			if(nl!=null && nl.getLength()>0){
				ConfigHolder.generalCfg.memAllocConfig.setMinPFF(getElementIntValue((Element)nl.item(0) , "minPFF" , -1));
				ConfigHolder.generalCfg.memAllocConfig.setMaxPFF(getElementIntValue((Element)nl.item(0) , "maxPFF" , -1));
				ConfigHolder.generalCfg.memAllocConfig.setNEvictedNodesToRun(getElementIntValue((Element)nl.item(0) , "evNodesToRun" , -1));

			}
			
			nl = rootNode.getElementsByTagName("cacheConfig");
			if (nl != null && nl.getLength() > 0) {
				for(int i = 0 ; i<nl.getLength() && i<ConfigHolder.numberCaches;i++){
					setCacheConfig(ConfigHolder.cacheCfgs[i], (Element) nl.item(i));	
				}
			}
			
			nl = rootNode.getElementsByTagName("mainMemoryConfig");
			if (nl != null && nl.getLength() > 0) {
				setCacheConfig(
						ConfigHolder.cacheCfgs[ConfigHolder.numberCaches],
						(Element) nl.item(0));
			}

			

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e2) {
			e2.printStackTrace();
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private static void setCacheConfig(CacheConfig cacheCfg, Element configElem) {
		cacheCfg.setNumberEntriesNBits(getElementIntValue(configElem,
				"numberEntriesNBits", 0));
		String value = getElementValue(configElem, "dataInstrSeparated" , "false");
		if (!"false".equals(value)) {
			cacheCfg.blockSizeNBits = new int[2];
		}
		else{
			cacheCfg.blockSizeNBits = new int[1];
		}
		cacheCfg.blockSizeNBits[0] = getElementIntValue(configElem,
				"blockSizeNBits", 0);
		if(cacheCfg.blockSizeNBits.length==2){
			cacheCfg.blockSizeNBits[1] = getElementIntValue(configElem,
					"blockSizeInstrNBits", 0);
		}

		value = getElementValue(configElem, "evictionPolicy" , "randomss");
		if (value != null) {
			if (value.equalsIgnoreCase("random")) {
				cacheCfg.setEvictionPolicy(CacheConfig.RANDOM_POLICY);
			} else if (value.equalsIgnoreCase("fifo")) {
				cacheCfg.setEvictionPolicy(CacheConfig.FIFO_POLICY);
			} else if (value.equalsIgnoreCase("lfu")) {
				cacheCfg.setEvictionPolicy(CacheConfig.LFU_POLICY);
			} else if (value.equalsIgnoreCase("lru")) {
				cacheCfg.setEvictionPolicy(CacheConfig.LRU_POLICY);
			} else if (value.equalsIgnoreCase("nfu")) {
				cacheCfg.setEvictionPolicy(CacheConfig.NFU_POLICY);
			} else if (value.equalsIgnoreCase("nru")) {
				cacheCfg.setEvictionPolicy(CacheConfig.NRU_POLICY);
			} else if (value.equalsIgnoreCase("opt")) {
				cacheCfg.setEvictionPolicy(CacheConfig.OPT_POLICY);
			} else if (value.equalsIgnoreCase("mru")) {
				cacheCfg.setEvictionPolicy(CacheConfig.MRU_POLICY);
			}
		}

		cacheCfg.setBusSize(getElementIntValue(configElem, "busSize", 0));
		cacheCfg.setAccessTimeUnits(getElementIntValue(configElem,
				"accessTimeUnits", 1));
		cacheCfg.setNumberSetsNBits(getElementIntValue(configElem,
				"numberSetsNBits", 0));
		//writeThrough or writeBack
		value = getElementValue(configElem, "hitWritePolicy" , "writeBack");
		cacheCfg.setWriteThroughHitPolicy(value
				.equalsIgnoreCase("writeThrough"));
		// writeAllocate or noWriteAllocate
		value = getElementValue(configElem, "missWritePolicy" , "writeAllocate");
		cacheCfg.setWriteAllocateMissPolicy(value
				.equalsIgnoreCase("writeAllocate") );

	}

	
	private static String getElementValue(Element parentElement,
			String elementName , String defValue) {
		NodeList tempList;
		Element tempElement;
		Text tempText;
		tempList = parentElement.getElementsByTagName(elementName);
		if (tempList != null && tempList.getLength() > 0) {
			tempElement = (Element) tempList.item(0);
			tempText = (Text) tempElement.getFirstChild();
			return tempText.getNodeValue();
		}
		return defValue;
	}

	private static int getElementIntValue(Element parentElement,
			String elementName, int defValue) {
		String value = getElementValue(parentElement, elementName , null);
		int intValue;
		try {
			intValue = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			intValue = defValue;
		}
		return intValue;
	}

}
