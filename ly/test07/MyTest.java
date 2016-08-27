package test07;

import java.util.Calendar;
import java.util.LinkedList;

import gridsim.GridResource;
import gridsim.GridSim;
import gridsim.Machine;
import gridsim.MachineList;
import gridsim.ResourceCharacteristics;

/**
 * ���Ǳ��������������˵�������ͬ��������Դ�ύ��ȡ�����ϵ���������ͣ���Ƴ�����
 * ����Ե����������е������������ת���࣬���磺totalUser��totalGridlet��
 */
public class MyTest {
	private static final int MIN=1;//����������Сֵ
	private static final int MAX=8;//�����������ֵ
	
	/**
	 * ��һ���ע�ͣ�����������case�ټӣ�
	 * 
	 * ��Щ���������ṩ�Ĳ����У�
	 * ����1���ύ��������-Ȼ��ȴ���������ִ�н������ֻ����Խ��
	 * ����2���ύ��������-ȡ������һЩ��������-���
	 * ����3���ύ��������-��ͣ����һЩ��������-ȡ��-���
	 * ����4���ύ��������-��ͣ-�ָ�-ȡ��-���
	 * ����5���ύ��������-�ƶ�����һЩ��������-���
	 * ����6���ύ��������-��ͣ-�ƶ�-���
	 * ����7���ύ��������-��ͣ-�ָ�-�ƶ�-���
	 * ����8���ύ��������-��ͣ-�ָ�-�ƶ�-ȡ��-���
	 * 
	 * ��ʾ��
	 * -����1����򵥵ģ�����8����ӵ�
	 * -��Щ���������Ƿǳ����ģ�����ζ�������ͨ�����ӻ������������totalUser��totalPE�Ȳ���
	 *  ������ʵ��ʹ֮��ú��Ӵ󡣶��㲻��Ҫ�޸Ĳ����������κ�һ���ࡣ
	 * -ע�⣬��Ҫ���������õ�̫�󣨳���200������Ϊ����ܻ��ڴ������
	 * -����ǽ�������������ֲʵ�飬����Ҫ����������Դʵ�������������˵����6����Դʵ��
	 */
	public static void main(String[] args) {
		System.out.println("��ʼ����~");
		try {
			//���������в�������һ����������������ʱ�乲���ǿռ乲��
			int policy=0;
			if(args[0].equals("t")||args[0].equals("time")){
				policy=ResourceCharacteristics.TIME_SHARED;
			}else if(args[0].equals("s")||args[0].equals("space")){
				policy=ResourceCharacteristics.SPACE_SHARED;
			}else{
				System.out.println("������Ч�ķ������");
				return;
			}
			
			//����ѡ���ĸ���������
			int testNum=Integer.parseInt(args[1]);
			if(testNum<MIN||testNum>MAX){//��Ӧ���ǣ�������Ч�����ֶ�ִ������1��
				testNum=MIN;
			}
			
            ////////////////////////////////////////
            // ��һ������ʼ��GridSim����Ӧ���ڴ����κ�ʵ��֮ǰ���ø÷�����
			// û�г�ʼ��֮ǰ�޷����иð�������õ�run-time�쳣��
			Calendar calendar=Calendar.getInstance();
			boolean trace_flag=false;//true˵������GridSim�¼�
			
			String[] exclude_from_file={""};
			String[] exclude_from_processing={""};
			
			String report_name=null;
			
			//��ʼ��������ر���
			double baudRate[]={1000, 5000};//ż���治ͬ�Ĵ���
			int peRating[]={10, 50};//ż���治ͬ��PE��
			double price[]={3.0, 5.0};//ż���治ͬ����Դ
			int gridletLength[]={1000, 2000, 3000, 4000, 5000};
			
			//��ʼ��GridSim��
			int totalUser=2;//������������û���
			GridSim.init(totalUser, calendar, trace_flag, exclude_from_file, exclude_from_processing, report_name);
			
            //////////////////////////////////////
            // �ڶ���������һ������������Դ����
			int totalResource=3;//���������������Դ����
			int totalMachine=1;//ÿ����Դ�Ļ�������
			int totalPE=3;//ÿ��������PE����
			createResource(totalResource, totalMachine, totalPE, baudRate, peRating, price, policy);
			
            /////////////////////////////////////
            // �����������������û�
			int totalGridlet=4;//ÿ���û���������������
			createUser(totalUser, totalGridlet, gridletLength, baudRate, testNum);
			
            ////////////////////////////////////
            // ���Ĳ�����ʼģ��
			GridSim.startGridSimulation();
		} catch (Exception e) {
			System.out.println("��������");
			System.out.println(e.getMessage());
			System.out.println("�÷���java Test [time | space] [1-8]");
		}
		System.out.println("============���������============");
	}
	
	/**
	 * �������������Դ
	 * @param totalRes	������Դ����������������Ϊ3
	 * @param totalMachine	ÿ����Դ�Ļ���������������1
	 * @param totalPE	ÿ��������PE������������3
	 * @param baudRate	������ż��ͬ��ż1000����5000
	 * @param peRating	PE�ʣ���ż��ͬ��ż10����50
	 * @param price	Ӧ����������cost�����йصĲ�����Ӧ����˵ʹ��ÿ����Դ�Ĵ��ۣ���ż��ͬ��ż3.0����5.0
	 * @param policy	ʱ�乲���ռ乲��
	 */
	public static void createResource(int totalRes, int totalMachine,
							int totalPE, double[] baudRate, int[] peRating,
							double[] price, int policy){
		double bandwidth=0;
		double cost=0.0;
		
		//ѭ������һ������������Դ
		for(int i=0; i<totalRes; i++){
			String name="GridResource_"+i;
			if(i%2==0){
				bandwidth=baudRate[0];
				cost=price[0];
			}else{
				bandwidth=baudRate[1];
				cost=price[1];
			}
			
			//����һ��������Դ
			createGridResource(name, totalMachine, totalPE, bandwidth, peRating, policy, cost);
		}
	}
	
	/**
	 * ������������û�
	 * @param totalUser	����ʵ����û�������������2
	 * @param totalGridlet	ÿ���û���������Դ������������4
	 * @param glLength	�������񳤶ȣ�һ��int���飬{1000, 2000, 3000, 4000, 5000}
	 * @param baudRate	������ż��ͬ��ż1000����5000
	 * @param testNum	����������Ĳ���������ţ�1-8��
	 */
	public static void createUser(int totalUser, int totalGridlet,
							int[] glLength, double[] baudRate, int testNum){
		try {
			double bandwidth=0;
			double delay=0.0;
			
			for(int i=0; i<totalUser; i++){
				String name="�û�_"+i;
				if(i%2==0){
					bandwidth=baudRate[0];
					delay=5.0;
				}else{
					bandwidth=baudRate[1];
				}
				
				//����һ�������û�����newһ������������
				createTestCase(name, bandwidth, delay, totalGridlet, glLength, testNum);
			}
		} catch (Exception e) {
			//����...
		}
	}
	
	/**
	 * ��ͬ����������ѡ����
	 * ����ߵĴ���Ӧ�õ�д�����в�����������д��
	 */
	private static void createTestCase(String name, double bandwidth,
							double delay, int totalGridlet, int[] glLength,
							int testNum) throws Exception{
		switch(testNum){
			case 1:
				new TestCase1(name, bandwidth, delay, totalGridlet, glLength);
				break;
				
			case 2:
				new TestCase2(name, bandwidth, delay, totalGridlet, glLength);
				break;
				
			case 3:
				new TestCase3(name, bandwidth, delay, totalGridlet, glLength);
				break;
				
			case 4:
				new TestCase4(name, bandwidth, delay, totalGridlet, glLength);
				break;
				
			case 5:
				new TestCase5(name, bandwidth, delay, totalGridlet, glLength);
				break;
				
			case 6:
				new TestCase6(name, bandwidth, delay, totalGridlet, glLength);
				break;
				
			case 7:
				new TestCase7(name, bandwidth, delay, totalGridlet, glLength);
				break;
				
			case 8:
				new TestCase8(name, bandwidth, delay, totalGridlet, glLength);
				break;
				
			default:
				System.out.println("����ʶ��Ĳ���������");
				break;
		}
	}
	
	/**
	 * ����һ��������Դ
	 */
	private static void createGridResource(String name, int totalMachine,
							int totalPE, double bandwidth, int[] peRating,
							int policy, double cost){
		MachineList mList=new MachineList();
		
		int rating=0;
		for(int i=0; i<totalMachine; i++){
			//������PE RatingҲ������ż������ͬ
			if(i%2==0){
				rating=peRating[0];
			}else{
				rating=peRating[1];
			}
			
			mList.add(new Machine(i, totalPE, rating));
		}
		
		String arch="Sun Ultra";
		String os="Solaris";
		double time_zone=0.0;
		
		ResourceCharacteristics resConfig=new ResourceCharacteristics(arch, os, mList, policy, time_zone, cost);
		
		long seed=11L*13*17*19*23+1;
		double peakLoad=0.0;
		double offPeakLoad=0.0;
		double holidayLoad=0.0;
		
		LinkedList<Integer> Weekends=new LinkedList<>();
		Weekends.add(new Integer(Calendar.SATURDAY));
		Weekends.add(new Integer(Calendar.SUNDAY));
		
		LinkedList<Integer> Holidays=new LinkedList<>();
		try {
			GridResource gridRes=new GridResource(name, bandwidth, seed, resConfig, peakLoad, offPeakLoad, holidayLoad, Weekends, Holidays);
		} catch (Exception e) {
			System.out.println("����������Դ����");
			System.out.println(e.getMessage());
		}
		
		System.out.println("����һ����Ϊ"+name+"��������Դ");
		return;//����б�Ҫ�𣿣���
	}
}
















