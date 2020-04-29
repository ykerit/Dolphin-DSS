package common.util;

import com.ceph.rados.IoCTX;
import com.ceph.rados.Rados;
import com.ceph.rados.exceptions.RadosException;

import com.ceph.rados.jna.RadosClusterInfo;
import common.service.AbstractService;
import config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CephService extends AbstractService {
	private static final Logger log = LogManager.getLogger(CephService.class.getName());
	private Rados cluster;
	private List<IoCTX> ioCTXList;
	private Configuration configuration;

	public CephService(Configuration configuration) {
		super(CephService.class.getName());
		ioCTXList = new ArrayList<>();
		this.configuration = configuration;
	}

	@Override
	protected void serviceInit() {
		cluster = new Rados("admin");
		File file = new File(configuration.getCephConfDir());
		log.debug("ceph path :{}", configuration.getCephConfDir());
		try {
			cluster.confReadFile(file);
		} catch (RadosException e) {
			e.printStackTrace();
		}
		super.serviceInit();
	}

	@Override
	protected void serviceStart() {
		try {
			cluster.connect();
		} catch (RadosException e) {
			e.printStackTrace();
		}
		super.serviceStart();
	}

	@Override
	protected void serviceStop() {
		for (IoCTX ioCTX : ioCTXList) {
			cluster.ioCtxDestroy(ioCTX);
		}
		ioCTXList.clear();
		cluster.shutDown();
		super.serviceStop();
	}

	public IoCTX getIoContext(String pool) throws RadosException {
		String[] poolList = cluster.poolList();
		IoCTX ioCTX = null;
		for (String var : poolList) {
			log.debug("pool: {}", var);
			if (var.equals(pool)) {
				ioCTX = cluster.ioCtxCreate(pool);
				ioCTXList.add(ioCTX);
				return ioCTX;
			}
		}
		cluster.poolCreate(pool);
		ioCTX = cluster.ioCtxCreate(pool);
		ioCTXList.add(ioCTX);
		return ioCTX;
	}

	public Map<String, Long> clusterStat() throws RadosException {
		RadosClusterInfo info = cluster.clusterStat();
		Map<String, Long> infos = new HashMap<>();
		infos.put("kb", info.kb);
		infos.put("kb_avail", info.kb_avail);
		infos.put("kb_used", info.kb_used);
		infos.put("num_objects", info.num_objects);
		return infos;
	}

}

