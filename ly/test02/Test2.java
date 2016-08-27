package test02;

import java.util.Random;

import gridsim.GridSimRandom;
import gridsim.GridSimStandardPE;
import gridsim.Gridlet;
import gridsim.GridletList;
import gridsim.ResourceUserList;

/*����example2����ϵ
 * ���б���ʱ�����ӡһ�������б�����ǵ����ԣ�����չʾ����δ�������Gridlet�������Բ���Ҫģ���κζ�����
 * ��������Ҫ��ʼ��GridSim��SimJava����Ϊ��������������ģ���ǲ���صġ�
 * 
 * ������һ���򵥵ĳ�����˵�����ʹ��GridSim����
 * 		����չʾ����δ���һ�����������û���һ�������û�����һ����������
 * 		��ˣ�����Ҳ��չʾ���ʹ�û�ʹ��GridSimRandom������������
 * 
 * ��ʾ���������õ���ֵ�Ǵ�GridSim paper����ȡ�ģ�http://www.gridbus.org/gridsim/��*/

/**�����չʾ����δ���һ�����������û������⣬Ҳ��������������Gridlet�Ĵ���*/
public class Test2 {
	/**
	 * ���б�����������
	 * */
	public static void main(String[] args) {
		System.out.println("��ʼ���������û�");
		System.out.println();
		
		try {
			//�������񼯺�
			GridletList list=createGridlet();
			System.out.println("������"+list.size()+"������");
			
			ResourceUserList userList=createGridUser(list);
			System.out.println("������"+userList.size()+"�������û�");
			
			//��ӡ�����б�
			printGridletList(list);
			System.out.println("��������~");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("��������");
		}
	}
	
	/**
	 * һ�������û��ж��Ҫ��������������񡣱�������չʾ��δ�����������ʹ�û�ʹ��GridSimRandom��
	 * */
	private static GridletList createGridlet(){
		//����һ��ʢ���������������
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
		Random random=new Random();
		
		//����PE��MIPS Rating
		GridSimStandardPE.setRating(100);
		
		//����5������������ɵ���5�������䳤�ȵȲ�����������ģ��Ժ�ģ��ʱ���õ��ϣ�
		int count=5;
		double min_range=0.10;
		double max_range=0.50;
		for(int i=1;i<count+1;i++){
			//���񳤶������ֵ�͵�ǰPE����������MIPS Rating������
			length=GridSimStandardPE.toMIs(random.nextDouble()*output_size);
			
			//�涨�������ļ��ĳ��ȵı仯��Χ�ǣ�100 + (10% to 50%)
			file_size=(long) GridSimRandom.real(100, min_range, max_range, random.nextDouble());
			
			//�涨������������ȵı仯��Χ�ǣ�250 + (10% to 50%)
			output_size=(long) GridSimRandom.real(250, min_range, max_range, random.nextDouble());
			
			//����һ���µ������������
			Gridlet gridlet=new Gridlet(id+i, length, file_size, output_size);
			
			//����������񵽼���
			list.add(gridlet);
		}
		
		return list;
	}
	
	/**
	 * ���������û����ڱ����У�����3���û���Ȼ�����Ƿ������������
	 */
	private static ResourceUserList createGridUser(GridletList list){
		ResourceUserList userList=new ResourceUserList();//ResourceUserList��̳���LinkedList
														//����ֻ������������һ����add������һ���ǹ�ʱ��myRemove������һ����super��LinkedList��remove����
		
		userList.add(0);//�û�id��0��ʼ
		userList.add(1);//�˴�����Ϊint���ͣ�Ȼ�������ڲ��Ὣ���װ��Integer����Ȼ����ӵ�����
		userList.add(2);//���������жϼ������Ƿ��Ѿ����ڸö������Ѵ����򷵻�false�����򽫶�����ӵ����ϣ�������true
		
		int userSize=userList.size();
		int gridletSize=list.size();
		int id=0;
		
		//���û�ID�����ָ������
		for(int i=0;i<gridletSize;i++){
			if(i!=0 && i%userSize==0){
				id++;
			}
				//���ַ��䷽ʽҲ��ͦ�ر�...012������û�0��345������û�1��67������û�2����Ӧ����û�õ�ʲô�����㷨��...
			((Gridlet)list.get(i)).setUserID(id);//����i������������id���û�
		}
		
		return userList;
	}
	
	private static void printGridletList(GridletList list){
		int size=list.size();
		Gridlet gridlet;
		
		String indent="	";//����
		System.out.println();
		System.out.println("Gridlet ID"+ indent+indent +"User ID"+ indent+indent +"length"+ indent+indent 
				+"file size"+ indent +"output size");
		
		for(int i=0;i<size;i++){
			gridlet=(Gridlet)list.get(i);//Ϊɶ��Ҫǿתһ�£�����
			System.out.println(indent+gridlet.getGridletID()+indent+
					indent+gridlet.getUserID()+indent+indent+
					(int)gridlet.getGridletLength()+indent+indent+
					(int)gridlet.getGridletFileSize()+indent+indent+
					(int)gridlet.getGridletOutputSize());
		}
	}
}




























