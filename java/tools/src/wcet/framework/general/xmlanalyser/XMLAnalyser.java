/**
 * 
 */
package wcet.framework.general.xmlanalyser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import wcet.framework.exceptions.InitException;
import wcet.framework.exceptions.SurplusComponentException;
import wcet.framework.exceptions.TaskInitException;
import wcet.framework.general.AbstractAnalyser;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.general.IDataStoreKeys;
import wcet.framework.interfaces.general.xmlanalyser.IXMLAnalyserKeys;
import wcet.framework.util.ClassLoaderSingleton;

/**
 * @author Elena Axamitova
 * @version 0.5
 */
public final class XMLAnalyser extends AbstractAnalyser {

    private ArrayList<IAnalyserComponent> mainComponents;

    private String xmlFile;

    private Properties arguments;

    private MyHandler myHandler;

    private ValidatorHandler myValidatorHandler;

    private IDataStore dataStore;

    public XMLAnalyser() {
	this.myHandler = new MyHandler();
	this.mainComponents = new ArrayList<IAnalyserComponent>();
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.interfaces.general.IAnalyser#init()
         */
    public void init(Properties arguments) throws InitException {
	this.arguments = arguments;
	this.xmlFile = arguments
		.getProperty(IXMLAnalyserKeys.ANALYSER_DESCRIPTION_FILE);
	if (this.xmlFile == null)
	    throw new InitException(
		    "XMLAnalyser: No xml analyser description file specified.");
	this.parseFile();
	this.componentRunner.setOutput(this.dataStore.getOutput());
	for (Iterator<IAnalyserComponent> iterator = this.mainComponents
		.iterator(); iterator.hasNext();) {
	    iterator.next().init();
	}
    }

    public void setTask(String mainClass) throws TaskInitException {
	this.dataStore.setTask(mainClass);
    }
    @Override
    protected void addComponent(IAnalyserComponent component) throws SurplusComponentException {
	this.mainComponents.add(component);
	super.addComponent(component);
    }

    private void parseFile() throws InitException {

	try {
	    SAXParser parser = new SAXParser();
	    this.initValidation();
	    parser.setContentHandler(this.myValidatorHandler);
	    parser.setErrorHandler(this.myHandler);
	    parser.parse(this.xmlFile);
	} catch (Exception e) {
	    throw new InitException(e);
	}
    }

    private void initValidation() throws InitException {
	try {
	    SchemaFactory schemaFactory = SchemaFactory
		    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	    Schema schema = schemaFactory.newSchema(new File(
		    "schema/basicSchema.xsd"));
	    myValidatorHandler = schema.newValidatorHandler();
	    myValidatorHandler.setErrorHandler(myHandler);
	    myValidatorHandler.setContentHandler(myHandler);
	} catch (Exception e) {
	    throw new InitException(e);
	}
    }

    private class MyHandler extends DefaultHandler {

	StringBuffer text = null;

	ArrayList<IAnalyserComponent> childComponents = null;

	ComponentTree currHelper = null;

	ClassLoader loader = null;

	IAnalyserComponent temp;

	MyHandler() {
	    this.text = new StringBuffer();
	    this.childComponents = new ArrayList<IAnalyserComponent>();
	    this.currHelper = new ComponentTree(null);
	    this.childComponents = this.currHelper.childComponents;
	    this.loader = ClassLoaderSingleton.getInstance();
	}

	@Override
	public void endElement(String uri, String localName, String qName)
		throws SAXException {
	    try {
		if (qName.equalsIgnoreCase("datastore")) {
		    this.loadDataStore(this.text.toString().trim());
		} else if (qName.equalsIgnoreCase("fullname")) {
		    this.currHelper.fullName = this.text.toString().trim();
		} else if (qName.equalsIgnoreCase("propertyfile")) {
		    this.loadProtertyFile(this.text.toString().trim());
		} else if (qName.equalsIgnoreCase("assembledComponent")) {
		    this.loadAssembledComponent(currHelper);
		    this.currHelper = currHelper.father;
		} else if (qName.equalsIgnoreCase("fixedComponent")) {
		    this.loadFixedComponent(currHelper);
		    // this.currHelper = currHelper.father;
		} else if (qName.equalsIgnoreCase("analyser")) {
		    ListIterator<IAnalyserComponent> iterator = this.childComponents
			    .listIterator();
		    while (iterator.hasNext()) {
			addComponent(iterator.next());
		    }
		}
	    } catch (Exception e) {
		throw new SAXException(e);
	    }
	    this.text.delete(0, text.length());
	}

	private void loadAssembledComponent(ComponentTree helper)
		throws InitException {
	    try {
		if (helper.order <= 0) {
		    throw new InitException(
			    "Bad assembled document declaration.");
		} else {
		    temp = new AssembledComponent(helper.onlyOne, helper.order);
		}
		if (helper.childComponents != null) {
		    ListIterator<IAnalyserComponent> iterator = helper.childComponents
			    .listIterator();
		    while (iterator.hasNext()) {
			((AssembledComponent) temp).addComponent(iterator
				.next());
		    }
		}
		helper.father.childComponents.add(temp);
	    } catch (Exception e) {
		throw new InitException(e);
	    }

	}

	private void loadFixedComponent(ComponentTree helper)
		throws InitException {
	    try {
		Class clazz = loader.loadClass(helper.fullName);
		Constructor constructor = clazz
			.getConstructor(new Class[] { Class
				.forName("wcet.framework.interfaces.general.IDataStore") });
		temp = (IAnalyserComponent) constructor
			.newInstance(new Object[] { dataStore });
		helper.childComponents.add(temp);
	    } catch (Exception e) {
		throw new InitException(e);
	    }

	}

	@Override
	public void error(SAXParseException e) throws SAXException {
	    throw e;
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
	    throw e;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
		Attributes attributes) throws SAXException {
	    if (qName.equalsIgnoreCase("assembledComponent")) {
		currHelper = new ComponentTree(this.currHelper);
		for (int i = 0; i < attributes.getLength(); i++) {
		    if (attributes.getQName(i).equalsIgnoreCase("order")) {
			currHelper.order = Integer.valueOf(
				attributes.getValue(i)).intValue();
		    } else if (attributes.getQName(i).equalsIgnoreCase(
			    "onlyOne")) {
			currHelper.onlyOne = Boolean.valueOf(
				attributes.getValue(i)).booleanValue();
		    }
		}
	    }/*
                 * else if (qName.equalsIgnoreCase("fixedComponent")) {
                 * currHelper = new ComponentTree(this.currHelper); }
                 */

	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
	    throw e;
	}

	@Override
	public void characters(char[] ch, int start, int length)
		throws SAXException {
	    this.text.append(ch, start, length);
	}

	public void endDocument() throws SAXException {

	}

	private void loadProtertyFile(String pfInXML) {
	    Properties argumentsInXML = new Properties();
	    if (pfInXML != null) {
		try {
		    argumentsInXML.loadFromXML(new FileInputStream(pfInXML));
		} catch (Exception e) {
		    System.err
			    .println("Bad property file in xml analyser specified:");
		    e.printStackTrace();
		}
	    }

	    String libraryPath = argumentsInXML
		    .getProperty(IDataStoreKeys.LIBRARYPATH_KEY);
	    if (libraryPath != null) {
		// new library specified, enhance classloader
		ClassLoaderSingleton.addLibrary(libraryPath);
		this.loader = ClassLoaderSingleton.getInstance();
	    }
	    // if specified, command line properties are used
	    argumentsInXML.putAll(arguments);
	    arguments = argumentsInXML;
	}

	private void loadDataStore(String datastoreString) throws InitException {
	    try {
		Class clazz = loader.loadClass(datastoreString);
		dataStore = (IDataStore) clazz.newInstance();
	    } catch (Exception e) {
		throw new InitException(e);
	    }
	    this.loadPropertiesToDataStore();
	}
	
	private void loadPropertiesToDataStore() throws InitException {
		// special handling of output
		String output = arguments.getProperty(IDataStoreKeys.OUTPUT_KEY);
		PrintStream ps = null;
		if (output != null) {
		    try {
			ps = new PrintStream(output);
		    } catch (FileNotFoundException e) {
			// default option - System.out used;
			ps = System.out;
		    }
		} else {
		    ps = System.out;
		}
		dataStore.setOutput(ps);
		arguments.remove(IDataStoreKeys.OUTPUT_KEY);

		// other objects with special handling
		String classpath = arguments.getProperty(IDataStoreKeys.CLASSPATH_KEY);
		dataStore.setClasspath(classpath);
		arguments.remove(IDataStoreKeys.CLASSPATH_KEY);

		String sourcepath = arguments
			.getProperty(IDataStoreKeys.SOURCEPATH_KEY);
		dataStore.setSourcepath(sourcepath);
		arguments.remove(IDataStoreKeys.SOURCEPATH_KEY);

		String mainMethodDescriptor = arguments
			.getProperty(IDataStoreKeys.MAINMETHOD_DESCRIPTOR_KEY);
		dataStore.setMainMethodDescriptor(mainMethodDescriptor);
		arguments.remove(IDataStoreKeys.MAINMETHOD_DESCRIPTOR_KEY);

		String mainMethodName = arguments
			.getProperty(IDataStoreKeys.MAINMETHOD_NAME_KEY);
		dataStore.setMainMethodName(mainMethodName);
		arguments.remove(IDataStoreKeys.MAINMETHOD_NAME_KEY);

		// all other objects
		Enumeration allPropNames = arguments.propertyNames();
		while (allPropNames.hasMoreElements()) {
		    String propName = allPropNames.nextElement().toString();
		    dataStore.storeObject(propName, arguments.getProperty(propName));
		}
	}
	
	private class ComponentTree {
	    private String fullName;

	    private int order = 0;

	    private boolean onlyOne = true;

	    ArrayList<IAnalyserComponent> childComponents;

	    ComponentTree father;

	    ComponentTree(ComponentTree f) {
		this.father = f;
		this.childComponents = new ArrayList<IAnalyserComponent>();
	    }
	}
    }
}
