package test03;

import java.util.Calendar;
import java.util.Random;

import gridsim.GridSim;
import gridsim.GridSimRandom;
import gridsim.GridSimStandardPE;
import gridsim.GridSimTags;
import gridsim.Gridlet;
import gridsim.GridletList;

/*
 * ������һ���򵥵ĳ������������ʹ��GridSim����
 * 		����չʾ������GridSimʵ��֮������λ�����ϵ�ġ�
 */

/**
 * Test3�ഴ���������񣬲����䷢������һ��GridSimʵ�壨Test�ࣩ
 */
class Test3 extends GridSim{
	private String entityName_;//������һ���û�ʵ�壬�����������񱻷��͵�Ŀ��ʵ��
	private GridletList list_;
	private GridletList receiveList_;//��Test�����յ������������б�
	
	/**
	 * ����һ���µ�Test3����
	 * @param name	ʵ������
	 * @param baudRate	ͨ���ٶ�
	 * @param list	һ�����������б�
	 * @throws Exception	������ʵ�����ڳ�ʼ��GridSim������ʵ�������ǿ�ʱ�������쳣
	 */
	public Test3(String name, double baudRate, GridletList list) throws Exception {
		super(name);
		this.list_=list;
		receiveList_=new GridletList();
		
		//����һ��Testʵ�壬��ֵ��"entityName"
		entityName_="Test";
		new Test(entityName_, baudRate);//����Test3����ʱ���ͻᴴ��Test���󣬶���baudRate��ͬ
	}
	
	/**
	 * ���GridSimʵ���ͨ������ĵķ���
	 */
	@Override
	public void body() {
		int size=list_.size();
		Gridlet obj, gridlet;
		
		//ѭ����ʹÿ�δ���һ���������񣬲����䷢�͵�����GridSimʵ��
		for(int i=0; i<size; i++){
			obj=(Gridlet)list_.get(i);
			System.out.println("Test3�ڲ�body()����=>���ڷ��͵�"+obj.getGridletID()+"������");
			
			//���ӳٵ���"entityName"ָ����GridSimʵ�巢��һ�����������õ�����GridSimTags.SCHEDULE_NOW��
			super.send(entityName_, GridSimTags.SCHEDULE_NOW, GridSimTags.GRIDLET_SUBMIT, obj);
			
			//�յ��ķ���������������
			gridlet=super.gridletReceive();
			
			System.out.println("Test3�ڲ�body()����=>���ڽ��յ�"+obj.getGridletID()+"������");
			
			//���յ�����������洢���µ����������б������
			receiveList_.add(gridlet);
		}
		
		//��"entityName"��Ӧ��GridSimʵ�巢��ģ��������ź�
		super.send(entityName_, GridSimTags.SCHEDULE_NOW, GridSimTags.END_OF_SIMULATION);
	}
	
	/**
	 * �õ����������б�
	 * @return	һ������������б�
	 */
	public GridletList getGridletList(){
		return receiveList_;
	}
	
	/**
	 * main����
	 */
	public static void main(String[] args) {
		System.out.println("Test3��ʼ");
		System.out.println();
		
		try {
			/*��һ������ʼ��GridSim����Ӧ���ڴ����κ�ʵ��֮ǰ���ø÷�����
			 * ���ǲ�����û�г�ʼ��GridSim֮ǰʹ��������Դ�������ᵼ��run-time�쳣*/
			
			int num_user=0;//�û�����Ҫ�������������У���Ϊ����Ҫ�����û�ʵ�壬���Խ�ֵ��Ϊ0
			Calendar calendar=Calendar.getInstance();//�ڲ���ʱ����������¼ģ��Ŀ�ʼ�ͽ���ʱ��
			boolean trace_flag=true;//һ�����Կ��أ�ֵΪ���ʾ��Ҫ���ټ�¼GridSimģ���ÿһ��
			
			//list of files or processing names to be excluded from any statistical measures
			//��ͳ�ƹ����У����������ڵ��ļ����ƺʹ������
			String[] exclude_from_file={""};
			String[] exclude_from_processing={""};
			
			String report_name=null;//�������ƣ���������Ҫд���棬���Բ��������õ�ReportWriter�������
			
			//��ʼ��GridSim��
			System.out.println("��ʼ��GridSim��");
			GridSim.init(num_user, calendar, trace_flag, exclude_from_file,
					exclude_from_processing, report_name);
			
			//�ڶ���������һ�����������б�
			GridletList list=createGridlet();
			System.out.println("���ڴ���"+list.size()+"����������");
			
			//������������Test3����
			Test3 obj=new Test3("Test3", 560.00, list);
			
			//���Ĳ�����ʼģ��
			GridSim.startGridSimulation();
			
			//���һ�����������ʱ��ӡ��������
			GridletList newList=obj.getGridletList();
			printGridletList(newList);
			
			System.out.println("Test3���н�����");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("��������");
		}
	}
	
	/**
	 * �÷�����չʾ��δ�����������Ӧ���Ǹ�Test1һ���ģ����в�ͬ��
	 * @return	һ�����������б����
	 */
	private static GridletList createGridlet(){
		//����һ������
		GridletList list=new GridletList();
		
		
		//���ǲ�ʹ��GridSimRandom���ֶ�����3������
		int id=0;
		double length=3500.0;
		long file_size=300;
		long output_size=300;
		Gridlet gridlet1=new Gridlet(id, length, file_size, output_size);
		id++;
		Gridlet gridlet2=new Gridlet(id, 5000, 500, 500);
		id++;
		Gridlet gridlet3=new Gridlet(id, 9000, 900, 900);
		
		//������洢������
		list.add(gridlet1);
		list.add(gridlet2);
		list.add(gridlet3);
		
		//����ʹ��GridSimRandom��GridSimStandardPE�ഴ��5������
		long seed=11L*13*17*19*23+1;
		Random random=new Random(seed);
				
		//����PE��MIPS Rating
		GridSimStandardPE.setRating(100);
		
		//����5����������
		int count=5;
		for(int i=1; i<count+1; i++){
			//���񳤶������ֵ�͵�ǰPE����������MIPS Rating������
			length=GridSimStandardPE.toMIs(random.nextDouble()*50);
			
			//�涨�������ļ��ĳ��ȵı仯��Χ�ǣ�100 + (10% to 40%)
			file_size=(long) GridSimRandom.real(100, 0.10, 0.40, random.nextDouble());
			
			//�涨������������ȵı仯��Χ�ǣ�250 + (10% to 50%)
			output_size=(long) GridSimRandom.real(250, 0.10, 0.50, random.nextDouble());
			
			//����һ���µ������������
			Gridlet gridlet=new Gridlet(id+i, length, file_size, output_size);
			
			//����������񵽼���
			list.add(gridlet);
		}
		
		return list;
	}
	
	private static void printGridletList(GridletList list){
		int size=list.size();
		Gridlet gridlet;
		
		String indent="	";
		System.out.println();
		System.out.println("==========������==========");
		System.out.println("��������ID"+indent+indent+"״̬");
		
		for(int i=0; i<size; i++){
			gridlet=(Gridlet)list.get(i);
			System.out.print(indent+gridlet.getGridletID()+indent);
			
			if(gridlet.getGridletStatus()==Gridlet.SUCCESS){
				System.out.println("�ɹ�");
			}
		}
	}
		
}




















