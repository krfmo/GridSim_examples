package test09;

import eduni.simjava.Sim_event;
import gridsim.ARPolicy;
import gridsim.AllocPolicy;
import gridsim.GridResource;
import gridsim.GridSim;
import gridsim.GridSimTags;
import gridsim.IO_data;
import gridsim.ResourceCalendar;
import gridsim.ResourceCharacteristics;

/**
 * ����һ���µ�������Դʵ�塣����ͨ����ע���±�ǩ��GISʵ��ķ�ʽִ����һ���򵥵Ĺ���
 * Ȼ��ӷ����߽���һ���±�ǩ������򵥵Ĵ�ӡһ����Ϣ��˵��ǩ�ѱ����ա�
 * 
 * ����GridSim���Լ���������Դʵ�壬��������ܻ�������¹��ܵ�ʵ�塣�����ǲ���Ҫ�޸��Ѵ��ڵ�
 * ����ʵ��Ĳ��裺
 * - ����һ�����࣬�̳���gridsim.GridResource��
 * - ��дregisterOtherEntity()������ʵ�ֽ��±�ǩע�ᵽGISʵ�幦��
 * - ��дprocessOtherEvent()������ʵ�ִ��������ʵ�崫�����±�ǩ
 * 
 * ע�⣺ȷ����ǩֵ���Ѵ��ڵ�GridSim��ǩֵ��ͬ����Ϊ�÷��������������á�
 */
public class NewGridResource extends GridResource{

	/**
	 * ����һ���µ�������Դʵ�塣�в�ͬ�ķ������ø��๹�췽����
	 * �ڱ����У��������ֻѡ��һ��������
	 */
	public NewGridResource(String name, double baud_rate,
			ResourceCharacteristics resource, ResourceCalendar calendar,
			ARPolicy policy) throws Exception {
		super(name, baud_rate, resource, calendar, policy);
	}
	
	/**
	 * ��д�÷���ʵ���±�ǩ���¹���
	 */
	@Override
	protected void processOtherEvent(Sim_event ev) {
		try {
			/*
			 * �õ�������ID
			 * ע�⣺Sim_event.get_data()Я������һ�����͵Ķ���������Я��һ��Gridlet��String���������͵Ķ��֣�
			 * 		��ȡ���ڷ����ߡ���ˣ���object����ת���ɾ�������ʱҪС�ġ��ڱ����У�������Ӧ�÷���һ��Integer����
			 */
			Integer obj=(Integer) ev.get_data();
			
			//�õ������ߵ�name
			String name=GridSim.getEntityName(obj.intValue());
			switch(ev.get_tag()){
			case Test9.HELLO:
				System.out.println(super.get_name()+":��"+name+"�յ�HELLO��ǩ��ʱ��Ϊ"+GridSim.clock());
				break;
				
			case Test9.TEST:
				System.out.println(super.get_name()+":��"+name+"�յ�TEST��ǩ��ʱ��Ϊ"+GridSim.clock());
				break;
				
			default:
				break;
			}
		} catch (Exception e) {
			System.out.println(super.get_name()+".processOtherEvent():�������쳣��");
		}
	}

	/**
	 * ��д�÷�����ע���±�ǩ��GISʵ�塣����Ҫ����һ����GISʵ���Ա㴦������±�ǩ
	 */
	@Override
	protected void registerOtherEntity() {
		int SIZE=12;
		
		//��ȡGISʵ��ID
		int gisID=GridSim.getGridInfoServiceEntityId();
		
		//��ȡGISʵ����
		String gisName=GridSim.getEntityName(gisID);
		
		//ע��HELLO��ǩ��GISʵ��
		System.out.println(super.get_name()+".registerOtherEntity():ע��HELLO��ǩ��GIS����"
				+gisName+"��ʱ��Ϊ"+GridSim.clock());
		
		super.send(super.output, GridSimTags.SCHEDULE_NOW, Test9.HELLO, 
				new IO_data(new Integer(super.get_id()), SIZE, gisID));
		
		//ע��HELLO��ǩ��GISʵ��
		System.out.println(super.get_name()+".registerOtherEntity():ע��TEST��ǩ��GIS����"
				+gisName+"��ʱ��Ϊ"+GridSim.clock());
		
		super.send(super.output, GridSimTags.SCHEDULE_NOW, Test9.TEST, 
				new IO_data(new Integer(super.get_id()), SIZE, gisID));
	}
}




















