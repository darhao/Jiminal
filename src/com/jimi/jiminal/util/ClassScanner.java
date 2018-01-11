package com.jimi.jiminal.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 类扫描器
 * 
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class ClassScanner {
	
	public List<String> classPaths;
	
	/**
	 * 根据包名把所有类获取（如果类文件在jar包里会尝试从jar包中获取）
	 * @param packagePath
	 * @return 类列表
	 */
	public static List<Class> searchClass(String packagePath){
		//创建返回集
		List<Class> classes = new ArrayList<Class>();
		if(ClassScanner.class.getResource("/") == null) {
			try {
				URI jarPath = ClassScanner.class.getProtectionDomain().getCodeSource().getLocation().toURI();
				System.out.println(jarPath.toString());
				// 然后把我们的包名basPach转换为路径名
				packagePath = packagePath.replace(".", "/");
				System.out.println(packagePath);
				JarFile jarFile = new JarFile(new File(jarPath));
				Enumeration<JarEntry> enumerations = jarFile.entries();
				while(enumerations.hasMoreElements()) {
					JarEntry entry = enumerations.nextElement();
					if(entry.getName().startsWith(packagePath) && entry.getName().endsWith(".class")) {
						System.out.println(entry.getName());
						String className = entry.getName().replace('/', '.');
						className = className.substring(0, className.length() - 6);
						Class cls = null;
						try {
							cls = Class.forName(className);
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
						classes.add(cls);
					}
				}
				jarFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}else {
			ClassScanner classScanner = new ClassScanner();
			classScanner.classPaths = new ArrayList<String>();
			// 先把包名转换为路径,首先得到项目的classpath
			String classpath = ClassScanner.class.getResource("/").getPath();
			// 然后把我们的包名basPach转换为路径名
			packagePath = packagePath.replace(".", File.separator);
			// 然后把classpath和basePack合并
			String searchPath = classpath + packagePath;
			classScanner.doPath(new File(searchPath));
			// 这个时候我们已经得到了指定包下所有的类的绝对路径了。我们现在利用这些绝对路径和java的反射机制得到他们的类对象
			for (String s : classScanner.classPaths) {
				// 把
				// D:\work\code\20170401\search-class\target\classes\com\baibin\search\a\A.class
				// 这样的绝对路径转换为全类名com.baibin.search.a.A
				s = s.replace(classpath.replace("/", "\\").replaceFirst("\\\\", ""), "").replace("\\", ".")
						.replace(".class", "");
				Class cls = null;
				try {
					cls = Class.forName(s);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				classes.add(cls);
			}
		}
		return classes;
	}

	/**
	 * 该方法会得到所有的类，将类的绝对路径写入到classPaths中
	 * 
	 * @param file
	 */
	private void doPath(File file) {
		if (file.isDirectory()) {// 文件夹
			// 文件夹我们就递归
			File[] files = file.listFiles();
			for (File f1 : files) {
				doPath(f1);
			}
		} else {// 标准文件
			// 标准文件我们就判断是否是class文件
			if (file.getName().endsWith(".class")) {
				// 如果是class文件我们就放入我们的集合中。
				classPaths.add(file.getPath());
			}
		}
	}
}
