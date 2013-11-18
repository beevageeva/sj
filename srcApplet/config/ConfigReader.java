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
import org.w3c.dom.Document;


import config.DirectMappedPageTableConfig;
import config.InverseMappedPageTableCfg;

public class ConfigReader {



	public static String exportXml(){
		StringBuffer sb=new StringBuffer();
		sb.append("<config>\n");
		sb.append(" <virtualAddressNBits>");
		sb.append(String.valueOf(ConfigHolder.generalCfg.getVirtualAddrNBits()));
		sb.append("</virtualAddressNBits>\n");
		sb.append(" <numberProcessesNBits>");
		sb.append(String.valueOf(ConfigHolder.generalCfg.getNumberProcessesNBits()));
		sb.append("</numberProcessesNBits>\n");
		sb.append(" <diskATU>");
		sb.append(String.valueOf(ConfigHolder.generalCfg.getDiskAccessTime()));
		sb.append("</diskATU>\n");
		//pagetable
		sb.append(" <pageTable direct=\"");
		 if(ConfigHolder.pageTableCfg.isDirectMapped()){
			//direct mapped
			sb.append("true\">\n");
			DirectMappedPageTableConfig dptcfg = (DirectMappedPageTableConfig) ConfigHolder.pageTableCfg
              .getAddCfg();
			int[] ols = dptcfg.getOffsetsLength();
			if(ols!=null && ols.length>0){
				sb.append("  <offsetLengths>\n");
				for(int i = 0; i<ols.length ;i++){
					sb.append("   <length>");
					sb.append(ols[i]);
					sb.append("</length>\n");
				}
				sb.append("  </offsetLengths>\n");
			}
		 sb.append("  <searchMethod>");
		 if(dptcfg.isSearchMethodTopDown()){
				sb.append("topdown");
		 }	
		else{
				sb.append("bottomup");
	
		}
		
		sb.append("</searchMethod>\n");
		}
		else{
			sb.append("false\">\n");
			sb.append("  <hashAnchorSizeNBits>");
		 
			InverseMappedPageTableCfg icfg = (InverseMappedPageTableCfg) ConfigHolder.pageTableCfg
							.getAddCfg();
			sb.append(String.valueOf(icfg.getHashAnchorSizeNBits()));
			sb.append("</hashAnchorSizeNBits>\n");
		}
		
		sb.append("  <tlbConfig>\n");
		sb.append(getCacheConfigXML(ConfigHolder.pageTableCfg.getTlbConfig()));
		sb.append("  </tlbConfig>\n");
		sb.append(" </pageTable>\n");

		if(ConfigHolder.generalCfg.pageAgingConfig!=null && ConfigHolder.generalCfg.pageAgingConfig.getPageAgingIncrease()!=-1){
			sb.append(" <pageAgingConfig>\n");
			sb.append("  <pageAgingIncrease>" + ConfigHolder.generalCfg.pageAgingConfig.getPageAgingIncrease()  +  "</pageAgingIncrease>\n");		
			sb.append("  <memRefToRun>" + ConfigHolder.generalCfg.pageAgingConfig.getMemRefToBeRun()  +  "</memRefToRun>\n");		
			sb.append(" </pageAgingConfig>\n");

			}
		if(ConfigHolder.generalCfg.memAllocConfig!=null && ConfigHolder.generalCfg.memAllocConfig.getMinPFF()!=-1){
			sb.append(" <memAllocConfig>\n");
			sb.append("  <minPFF>" + ConfigHolder.generalCfg.memAllocConfig.getMinPFF()  +  "</minPFF>\n");		
			sb.append("  <maxPFF>" + ConfigHolder.generalCfg.memAllocConfig.getMaxPFF()  +  "</maxPFF>\n");		
			sb.append(" </memAllocConfig>\n");

			}
			
			




		for(int i = 0;i<ConfigHolder.numberCaches;i++){
			sb.append("\t<cacheConfig>\n");
			sb.append(getCacheConfigXML(ConfigHolder.cacheCfgs[i]));
			sb.append("\t</cacheConfig>\n");
		}
		sb.append("\t<mainMemoryConfig>\n");
		sb.append(getCacheConfigXML(ConfigHolder.cacheCfgs[ConfigHolder.numberCaches]));
		sb.append("\t</mainMemoryConfig>\n");
		sb.append("</config>");
		return sb.toString();
	}




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



	private static String getCacheConfigXML(CacheConfig cacheCfg){
		StringBuffer sb = new StringBuffer();
		sb.append("<numberEntriesNBits>" + cacheCfg.getNumberEntriesNBits()  + "</numberEntriesNBits>\n");
		sb.append("<blockSizeNBits>" + cacheCfg.blockSizeNBits[0]  + "</blockSizeNBits>\n");
		if (cacheCfg.isDataInstrSeparated()){
			sb.append("<dataInstrSeparated>true</dataInstrSeparated>\n");
			sb.append("<blockSizeInstrNBits>" + cacheCfg.blockSizeNBits[1]  + "</blockSizeInstrNBits>\n");
		}
		else{
			sb.append("<dataInstrSeparated>false</dataInstrSeparated>\n");
		}
		sb.append("<evictionPolicy>");
		short value = cacheCfg.getEvictionPolicy();
		switch (value) {
			case CacheConfig.RANDOM_POLICY:
				sb.append("RANDOM");
				break;
			case CacheConfig.FIFO_POLICY:
				sb.append("FIFO");
				break;
			case CacheConfig.LFU_POLICY:
				sb.append("LFU");
				break;
			case CacheConfig.LRU_POLICY:
				sb.append("LRU");
				break;
			case CacheConfig.NFU_POLICY:
				sb.append("NFU");
				break;
			case CacheConfig.NRU_POLICY:
				sb.append("NRU");
				break;
			case CacheConfig.OPT_POLICY:
				sb.append("OPT");
				break;
			case CacheConfig.MRU_POLICY:
				sb.append("MRU");
				break;
		}
		sb.append("</evictionPolicy>");

		sb.append("<busSize>" + cacheCfg.getBusSize()  + "</busSize>\n");
		sb.append("<accessTimeUnits>" + cacheCfg.getAccessTimeUnits()  + "</accessTimeUnits>\n");

		sb.append("<numberSetsNBits>" + cacheCfg.getNumberSetsNBits()  + "</numberSetsNBits>\n");
		sb.append("<hitWritePolicy>" + (cacheCfg.isWriteThroughHitPolicy() ? "writeThrough" : "writeBack")  + "</hitWritePolicy>\n");
		sb.append("<missWritePolicy>" + (cacheCfg.isWriteAllocateMissPolicy() ? "writeAllocate" : "noWriteAllocate")  + "</missWritePolicy>\n");
		return sb.toString();
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

		value = getElementValue(configElem, "evictionPolicy" , "random");
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
