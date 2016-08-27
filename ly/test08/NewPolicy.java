package test08;

import eduni.simjava.Sim_event;
import eduni.simjava.Sim_system;
import gridsim.AllocPolicy;
import gridsim.GridSimTags;
import gridsim.Gridlet;

/**
 * �������̳�AllocPolicy�࣬��ʵ��5�����󷽷���
 * �ڱ����У���չʾ�����һ���ύ��������Դ������������ȡ����ϵ
 */
public class NewPolicy extends AllocPolicy{

	/**
	 * ������Դ����ʵ�����Ĺ��췽��
	 * @param resName
	 * @param entityName
	 * @throws Exception
	 */
	protected NewPolicy(String resName, String entityName) throws Exception {
		//���봫�ݻظ���
		super(resName, entityName);
		System.out.println("���ڴ��� "+entityName);
	}

	/**
	 * �������һ����������Ȼ����״̬��ΪSUCCESS��
	 * Ȼ���������Ҫ�򴫵�һ��ack��Ȼ������������󴫻ظ������ߡ�
	 */
	@Override
	public void gridletSubmit(Gridlet gl, boolean ack) {
		System.out.println();
		System.out.println("NewPolicy.gridletSubmit():��������....");
		System.out.println("���ڽ�����������#"+gl.getGridletID());
		
		try {
			gl.setGridletStatus(Gridlet.SUCCESS);
		} catch (Exception e) {
			// ...����
		}
		
		//�����Ҫ�ظ�ack
		if(ack==true){
			System.out.println("NewPolicy.gridletSubmit():����һ��ack");
			
			//����һ��ack��˵���ò����ɹ����
			super.sendAck(GridSimTags.GRIDLET_SUBMIT_ACK, true, gl.getGridletID(), gl.getUserID());
		}
		
		System.out.println("NewPolicy.gridletSubmit():��������#"+gl.getGridletID()+"���û�#"+gl.getUserID());
		
		//������������󴫻ظ������û���ӵ���ߣ�
		super.sendFinishGridlet(gl);
	}

	@Override
	public void gridletCancel(int gridletId, int userId) {
		//...�Լ��Ĵ���ʵ�ָù���
	}

	@Override
	public void gridletPause(int gridletId, int userId, boolean ack) {
		//...�Լ��Ĵ���ʵ�ָù���
	}

	@Override
	public void gridletResume(int gridletId, int userId, boolean ack) {
		//...�Լ��Ĵ���ʵ�ָù���
	}

	@Override
	public int gridletStatus(int gridletId, int userId) {
		//...�Լ��Ĵ���ʵ�ָù���
		return 1;
	}

	@Override
	public void gridletMove(int gridletId, int userId, int destId, boolean ack) {
		//...�Լ��Ĵ���ʵ�ָù���
	}

	/**
	 * �÷�������Ҫ��ͼ�Ǵ����ڲ��¼������磺
	 * �����͵�������ͬһ��ʵ����¼���
	 * ����Ҫ���ݵ���һ��ʱ�䱣���ߵĽ�ɫ����ΪGridSim��һ����ɢ�¼���ģ����
	 */
	@Override
	public void body() {
		//ֻѰ���ڲ��¼���ѭ��
		Sim_event ev=new Sim_event();
		while(Sim_system.running()){
			super.sim_get_next(ev);
			
			//����������������ѭ��
			if(ev.get_tag()==GridSimTags.END_OF_SIMULATION||super.isEndSimulation()==true){
				break;
			}
		}
		
		//���ȷ���Ƿ��д�������ڲ��¼�
		while(super.sim_waiting()>0){
			//�ȴ��¼������ԡ�
			//��Ϊ����������������̿������ڲ��¼������й�
			super.sim_get_next(ev);
			System.out.println(super.resName_+".NewPolicy.body():�����ڲ��¼�");
		}
	}
	
}

















