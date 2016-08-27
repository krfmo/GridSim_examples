package test09;

/*
 * ����������չʾ��δ����������Լ���������Դ��������Ϣ����ʵ�塣
 */
import java.util.Calendar;
import java.util.LinkedList;

import gridsim.GridSim;
import gridsim.GridSimTags;
import gridsim.IO_data;
import gridsim.Machine;
import gridsim.MachineList;
import gridsim.ResourceCalendar;
import gridsim.ResourceCharacteristics;

public class Test9 extends GridSim{

	public static final int HELLO=900;
	public static final int TEST=901;
	
	private Integer ID_;//�ö����ʵ��ID
	private String name_;//�ö����ʵ����
	private int totalResource_;//�����õ�������Դ����
	public Test9(String name, double baudRate, int total_resource) throws Exception {
		super(name, baudRate);
		this.name_=name;
		this.totalResource_=total_resource;
		
		this.ID_=new Integer(getEntityId(name));
		System.out.println("���ڴ�����Ϊ"+name+"��idΪ"+this.ID_+"�������û�ʵ��");
	}
	
	public void body(){
		int resourceID[]=new int[this.totalResource_];
		String resourceName[]=new String[this.totalResource_];
		
		LinkedList resList;
		ResourceCharacteristics resChar;
		
		/*
		 * �ȴ���ȡһ����Դ�б�GridSim���̣߳�������Ҫ�ȵȴ�
		 */
		while(true){
			super.gridSimHold(1.0);
			
			resList=getGridResourceList();
			if(resList.size()==this.totalResource_){
				break;
			}else{
				System.out.println(this.name_+":���ڵȴ���ȡ��Դ�б�...");
			}
		}
		
		int SIZE=12;//Integer�����Լռ12�ֽ�
		int i=0;
		
		/*
		 * ѭ���õ����п�����Դ��
		 * һ����Դ��ʶ�����䷢��HELLO��TEST��ǩ
		 */
		for(i=0; i<this.totalResource_; i++){
			//��Դ�б�洢������ԴID��������Դ����
			resourceID[i]=((Integer)resList.get(i)).intValue();
			
			//����Դʵ��ķ��������Ե�����ע�⣬ֱ�ӷ��ͣ�����Ҫʹ��I/O�˿�
			super.send(resourceID[i], GridSimTags.SCHEDULE_NOW, GridSimTags.RESOURCE_CHARACTERISTICS, this.ID_);
			
			//�ȴ���ȡһ����Դ����
			resChar=(ResourceCharacteristics) receiveEventObject();
			resourceName[i]=resChar.getResourceName();
			
			//��ӡ��ʵ���յ���һ���ض�����Դ����
			System.out.println(this.name_+":�յ���Ϊ"+resourceName[i]+"��idΪ"+resourceID[i]+"����Դ���͵���Դ����");
			
			//����TEST��ǩ����Դ��ʹ��I/O�˿ڡ�
			//�ڻ������ϴ��䣬Ӧ���Ǵ���ʱ��
			System.out.println(this.name_+":���ڷ���TEST��ǩ����Դ"+resourceName[i]+"��ʱ��Ϊ"+GridSim.clock());
			super.send(super.output, GridSimTags.SCHEDULE_NOW, TEST,
					new IO_data(ID_, SIZE, resourceID[i]));
			
			//����HELLO��ǩ����Դ��ʹ��I/O�˿ڡ�
			System.out.println(this.name_+":���ڷ���HELLO��ǩ����Դ"+resourceName[i]+"��ʱ��Ϊ"+GridSim.clock());
			super.send(super.output, GridSimTags.SCHEDULE_NOW, HELLO,
					new IO_data(ID_, SIZE, resourceID[i]));
		}
		
		//��Ҫ�ȴ�10��������Դ�����յ����¼�
		super.sim_pause(10);
		
		//���ֹر�
		shutdownGridStatisticsEntity();
		shutdownUserEntity();
		terminateIOEntities();
		System.out.println(this.name_+":%%%%%%�˳�body()%%%%%%");
	}

	////////////��̬����////////////
	
	public static void main(String[] args) {
		System.out.println("��ʼTest9");
		
		try {
			//��һ������ʼ��GridSim���������ڴ����κ�ʵ��֮ǰ���ó�ʼ��������
			int num_user=1;//�����û�����
			Calendar calendar=Calendar.getInstance();
			boolean trace_flag=true;//true��ζ��׷��GridSim�¼�
			
			//��ʼ����
			System.out.println("��ʼ��GridSim��");
			
			//�ڱ����У���ʼ��GridSim��������һ��Ĭ�ϵ�GISʵ��
			GridSim.init(num_user, calendar, trace_flag, false);
			
			//����һ����GISʵ��
			NewGIS gis=new NewGIS("NewGIS");
			
			//��Ҫ�ڿ�ʼģ��ǰ�������������
			GridSim.setGIS(gis);
			
			//�ڶ���������һ������������Դʵ��
			NewGridResource resource0=createGridResource("Resource_0");
			int total_resource=1;
			
			//������������һ�����������û�ʵ��
			Test9 user0=new Test9("User_0", 560.00, total_resource);
			
			//���Ĳ�����ʼģ��
			GridSim.startGridSimulation();
			System.out.println("Test9����~");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("��������");
		}
	}
	
	/**
	 * ����һ��������Դ��
	 * @param name	��Դ��
	 * @return	һ��������Դ����
	 */
	private static NewGridResource createGridResource(String name){
		MachineList mList=new MachineList();
		
		int mipsRating=377;
		mList.add(new Machine(0, 4, mipsRating));
		
		String arch="Sun Ultra";
		String os="Solaris";
		double time_zone=9.0;
		double cost=3.0;
		
		ResourceCharacteristics resConfig=new ResourceCharacteristics(
				arch, os, mList, ResourceCharacteristics.SPACE_SHARED,
				time_zone, cost);
		
		double baud_rate=100.0;
		long seed=11L*13*17*19*23+1;
		double peakLoad=0.0;
		double offPeakLoad=0.0;
		double holidayLoad=0.0;
		
		LinkedList Weekends=new LinkedList();
		Weekends.add(new Integer(Calendar.SATURDAY));
		Weekends.add(new Integer(Calendar.SUNDAY));
		
		LinkedList Holidays=new LinkedList();
		
		ResourceCalendar calendar=new ResourceCalendar(time_zone, peakLoad,
				offPeakLoad, holidayLoad, Weekends, Holidays, seed);
		
		NewGridResource gridRes=null;
		try {
			//����Ĵ��봴��һ��NewGridResource������������ĸ������
			gridRes=new NewGridResource(name, baud_rate, resConfig, calendar, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("����һ����Ϊ"+name+"��������Դ");
		return gridRes;
	}
}



















