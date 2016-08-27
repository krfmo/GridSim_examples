package test07;

import java.util.LinkedList;

import gridsim.GridSim;
import gridsim.Gridlet;
import gridsim.GridletList;

/**
 * �ò��԰����ǹ����ύ-��ͣ-�ָ�-�ƶ�-���
 */
class TestCase7 extends GridSim{
	private int myId_;
	private String name_;
	private GridletList list_;
	private GridletList receiveList_;
	private double delay_;
	
	/**
	 * ����һ��TestCase7����
	 * @param name	�ö����ʵ����
	 * @param baudwidth	ͨ���ٶ�
	 * @param delay	ģ����ʱ
	 * @param totalGridlet	Ӧ�ô�����������������
	 * @param glLength	һ���洢��ͬ�������񳤶ȵ�����
	 * @throws Exception	�ڳ�ʼ��GridSim��֮ǰ������ʵ�壬��ʵ����Ϊ��ʱ�����쳣
	 */
	TestCase7(String name, double baudwidth, double delay, int totalGridlet,
			int[] glLength) throws Exception {
		super(name, baudwidth);
		this.name_=name;
		this.delay_=delay;
		
		this.receiveList_=new GridletList();
		this.list_=new GridletList();
		
		//Ϊ��ʵ���ȡID
		this.myId_=super.getEntityId(name);
		System.out.println("����һ����Ϊ"+name+"�������û�ʵ�壬id="+this.myId_);
		
		//Ϊ�����û�����һ���������������б�
		System.out.println(name+":���ڴ���"+totalGridlet+"����������");
		this.createGridlet(myId_, totalGridlet, glLength);
	}
	
	/**
	 * ����GridSimʵ���ͨ�ŵĺ��ķ���
	 */
	public void body() {
		//��������Դʵ��һЩʱ��ȥ��GISʵ��ע���Լ��ķ���
		super.gridSimHold(3.0);
		LinkedList<Integer> resList=super.getGridResourceList();//��ԭ��������˸�����
		
		//��ʼ����������
		int totalResource=resList.size();
		int resourceID[]=new int[totalResource];
		String resourceName[]=new String[totalResource];
		
		//��ȡ���п�����Դ��ѭ��
		int i=0;
		for(i=0; i<totalResource; i++){
			//��Դ�б��������ԴID���б�
			resourceID[i]=((Integer)resList.get(i)).intValue();
			
			//ͬʱҲ��ȡ���ǵ�����
			resourceName[i]=GridSim.getEntityName(resourceID[i]);
		}
		
        ////////////////////////////////////////////////
        // SUBMIT Gridlets
		
		//����Ҫ���͵��ĸ�������Դ
		int index=myId_%totalResource;
		if(index>=totalResource){
			index=0;
		}
		
		//�������е���������
		Gridlet gl=null;
		boolean success;
		for(i=0; i<list_.size(); i++){
			gl=(Gridlet)list_.get(i);
			
			//ż�����������񣬷��Ͳ�Я��ACK
			if(i%2==0){
				success=super.gridletSubmit(gl, resourceID[index], 0.0, true);
				System.out.println(name_+":���ڷ���״̬Ϊ"+success+"����������#"+gl.getGridletID()+"����Դ"+resourceName[index]);
			}else{
				//�������������񣬷��Ͳ�Я��ACK
				success=super.gridletSubmit(gl, resourceID[index], 0.0, false);
				System.out.println(name_+":���ڷ�����������#"+gl.getGridletID()+"����Դ"+resourceName[index]+"û��ACK������״̬Ϊ"+success);
			}
		}
		
        ///////////////////////////////////////////////////////////
        // PAUSING Gridlets
		
		//��ͣһ��ʱ��
		super.gridSimHold(15);
		System.out.println("<<<<<<<<<<��ͣ15����λ>>>>>>>>>>");
		
		//��ȷ�ϵ���ͣһ����������
		for(i=0; i<list_.size(); i++){
			if(i%3==0){
				success=super.gridletPause(i, myId_, resourceID[index], 0.0, true);
				System.out.println(name_+":��ͣ����#"+i+"��ʱ��Ϊ"+GridSim.clock()+"��success="+success);
			}
		}
		
        ///////////////////////////////////////////////////////////
        // RESUMING Gridlets
		
		//��ͣһ��ʱ��
		super.gridSimHold(15);
		System.out.println("<<<<<<<<<<��ͣ15����λ>>>>>>>>>>");
		
		//�ָ�һ����������
		for(i=0; i<list_.size(); i++){
			if(i%3==0){
				success=super.gridletResume(i, myId_, resourceID[index], 0.0, true);
				
				System.out.println(name_+":�ָ�����#"+i+"��ʱ��Ϊ"+GridSim.clock()+"��success="+success);
			}
		}
		
		//////////////////////////////////////////
		// MOVES Gridlets
		
		//��ͣһ��ʱ��
		super.gridSimHold(45);
		System.out.println("<<<<<<<<<<��ͣ45����λ>>>>>>>>>>");
		
		//���ȼ���Ƿ����㹻��������Դ
		if(resourceID.length==1){
			System.out.println("�����ƶ�����������Ϊ��Դֻ��һ��");
		}else{
			//�о��ǽ�����������Դ��λ��2�Ƴ�1��3�Ƴ�2�� ... ��1�Ƴ����һ��
			int move=0;
			if(index==0){
				move=resourceID.length-1;
			}else{
				move=index-1;
			}
			
			//ֻת��ѡ������������
			for(i=0; i<list_.size(); i++){
				if(i%3==0){
					success=super.gridletMove(i, myId_, resourceID[index], resourceID[move], 0, true);
					System.out.println(name_+":�ƶ���������#"+i+"ʱ��="+GridSim.clock()+"success="+success);
				}
			}
		}
		
		////////////////////////////////////////////////////////
		// RECEIVES Gridlets back
		
		//��ͣ��ʱ���һЩ����Ϊ����һ����С�Ĵ�����˵����������ĳ���̫����...
		super.gridSimHold(1000);
		System.out.println("<<<<<<<<<<��ͣ1000����λ>>>>>>>>>>");
		
		//�ջ���Щ��������
		int size=list_.size()-receiveList_.size();
		for(i=0; i<size; i++){
			gl=(Gridlet)super.receiveEventObject();//�õ�����������
			receiveList_.add(gl);//��ӵ�received list
			
			System.out.println(name_+":�յ���������#"+gl.getGridletID()+"����ʱ��Ϊ��"+GridSim.clock());
		}
		
		System.out.println(this.name_+":%%%%�˳�body()��ʱ��Ϊ"+GridSim.clock());
		
		//���ֹر�
		shutdownUserEntity();
		terminateIOEntities();
		
		//��ӡģ�����
		printGridletList(receiveList_, name_);
	}
	
	/**
	 * ��δ���һЩ��������ķ���
	 * @param userID
	 * @param numGridlet
	 * @param data
	 */
	private void createGridlet(int userID, int numGridlet, int[] data){
		int k=0;
		for(int i=0; i<numGridlet; i++){
			if(k==data.length){
				k=0;
			}
			
			//����һ����������
			Gridlet gl=new Gridlet(i, data[k], data[k], data[k]);
			gl.setUserID(userID);
			this.list_.add(gl);
			
			k++;
		}
	}
	
	/**
	 * ��ӡ�����������
	 * @param list
	 * @param name
	 */
	private void printGridletList(GridletList list, String name){
		int size=list.size();
		Gridlet gridlet=null;
		
		String indent="	";
		System.out.println();
		System.out.println("==========�û�"+name+"�����==========");
		System.out.println("����ID"+indent+"״̬"+indent+indent+"��ԴID"+indent+"����");
		
		//��ӡȫ�������ѭ��
		int i=0;
		for(i=0; i<size; i++){
			gridlet=(Gridlet)list.get(i);
			System.out.print(gridlet.getGridletID()+indent+gridlet.getGridletStatusString());
			if(gridlet.getGridletStatusString().equals("Success")){
				System.out.print(indent);
			}
			System.out.println(indent+gridlet.getResourceID()+indent+gridlet.getProcessingCost());
		}
		
		//��ӡÿһ������������ʷ��ѭ��
		for(i=0; i<size; i++){
			gridlet=(Gridlet)list.get(i);
			System.out.println(gridlet.getGridletHistory());
			
			System.out.println("����#"+gridlet.getGridletID()+"������="+gridlet.getGridletLength()
					+"����ɳ̶�="+gridlet.getGridletFinishedSoFar());
			System.out.println("==============================\n");
		}
	}
	
}
