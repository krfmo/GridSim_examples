package test03;

import eduni.simjava.Sim_event;
import gridsim.GridSim;
import gridsim.GridSimTags;
import gridsim.Gridlet;

/*
 * ������һ���򵥵ĳ������������ʹ��GridSim����
 * 		����չʾ������GridSimʵ��֮������λ�����ϵ�ġ�
 */

/**
 * Test�ཨ����Input��Outputʵ�塣Ȼ��Test������¼�ģ�⣬
 * �ȴ����մ���һ��GridSimʵ�壨��������Test3�ࣩ��������������
 * Ȼ�󣬸�����Test3�ഫ����������
 */
public class Test extends GridSim{

	/**
	 * ����һ���µ�Test����
	 * @param name	ʵ�������
	 * @param baudRate	ͨ���ٶ�
	 * @throws Exception	������ʵ���ڳ�ʼ��GridSim��֮ǰ������ʵ�������ǿ�ʱ�����׳��쳣
	 * @see gridsim.GridSim#Init(int, Calendar, boolean, String[], String[],
     *          String)
	 */
	public Test(String name, double baudRate) throws Exception {
		/*
		 * ���baud_rate����֪�ģ�����Ҫ����Input��Outputʵ�塣
		 * GridSim���ڵ���super()ʱ������
		 */
		super(name, baudRate);
		System.out.println("...���ڴ���һ���µĲ�����ʵ��");
	}
	
	/**
	 * һ�δ���һ���¼�������һ��GridSimʵ�����һ�������������
	 * �������������״̬��Ȼ����ش��������ߡ�
	 */
	@Override
	public void body() {
		int entityID;
		Sim_event ev=new Sim_event();
		Gridlet gridlet;
		
		//һ�δ���һ���¼�
		for(sim_get_next(ev);ev.get_tag()!=GridSimTags.END_OF_SIMULATION;
				sim_get_next(ev)){
			//�õ���Test3�õ��������������
			gridlet=(Gridlet) ev.get_data();
			
			//�������������״̬����ζ�Ÿ����������Ѿ����ɹ�����
			try {
				gridlet.setGridletStatus(Gridlet.SUCCESS);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			System.out.println("...Test���е�body()����=>��Test3������յ�IDΪ"+gridlet.getGridletID()+"����������");
			
			//�õ�������ID������Test3��
			entityID=ev.get_src();
			
			//�����ĺ���������񷢻ظ�������
			super.send(entityID, GridSimTags.SCHEDULE_NOW, GridSimTags.GRIDLET_RETURN, gridlet);
		}
		
		//����������������������ʵ��
		super.terminateIOEntities();
	}
	
}























