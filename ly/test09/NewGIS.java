package test09;

import eduni.simjava.Sim_event;
import gridsim.GridInformationService;
import gridsim.GridSim;
import gridsim.GridSimTags;

/**
 * һ���µ�������Ϣ����GIS��ʵ��
 * ����GridSimӵ���Լ���GISʵ�壬��������ܻ���
 * ��ʵ�����һЩ�¹��ܡ��������ڲ��ı��Ѵ��ڵ�GISʵ�������
 * ����Ҫ�Ĳ��裺
 * - ����һ������̳���gridsim.GridInformationService��
 * - ��дprocessOtherEvent���������������±�ǩ
 *   ע�⣺ȷ����ǩֵ���Ѵ��ڵ�GridSim��ǩ��ͬ����Ϊ�÷����ᱻ������
 *   
 * ���и�ʵ�壺
 * - ���������У�ʹ�ó�ʼ������GridSim.init(...)������booleanֵgis����Ϊfalse
 *   ע�⣺gis��������Ϊtrue��ʾʹ��Ĭ�ϵĻ��Ѵ��ڵ�GISʵ����������Լ���ʵ��
 * - ���������ڣ�����һ����ʵ��������磺NewGIS gisEntity=new NewGIS("NewGIS");
 * 
 * - ���������ڣ�ʹ��GridSim.setGIS(gisEntity)���洢���GISʵ�塣
 *   ע�⣺�÷���Ӧ�������з���֮ǰ���ã����ڵ���GridSim.startGridSimulation()����ǰ����
 */
public class NewGIS extends GridInformationService{

	/**
	 * ����һ����GISʵ��
	 * @param name	�����и����������Ĺ��췽�����ڶ��������Ǵ������������ｫ������ΪĬ��ֵ�ˣ����в���Ҫ�Ӵ��������
	 * @throws Exception
	 */
	public NewGIS(String name) throws Exception {
		super(name, GridSimTags.DEFAULT_BAUD_RATE);
	}
	
	/**
	 * ��д�÷�����ʵ���±�ǩ���¹���
	 * ע�⣺GISʵ��������ʵ��֮���ͨ�ű���ͨ��I/O�˿ڡ�������Ϣ���ο�gridsim.GridSimCore API
	 */
	@Override
	protected void processOtherEvent(Sim_event ev) {
		int resID=0;		//������ID
		String name=null;	//������name
		
		switch(ev.get_tag()){
		case Test9.HELLO:
			resID=((Integer)ev.get_data()).intValue();
			name=GridSim.getEntityName(resID);
			name=GridSim.getEntityName(resID);
			System.out.println(super.get_name()+":����Դ"+name+"�յ�HELLO��ǩ��ʱ��Ϊ"+GridSim.clock());
			break;
			
		case Test9.TEST:
			resID=((Integer)ev.get_data()).intValue();
			name=GridSim.getEntityName(resID);
			System.out.println(super.get_name()+":����Դ"+name+"�յ�TEST��ǩ��ʱ��Ϊ"+GridSim.clock());
			break;
			
		default:
			break;	
		}
	}

}
















