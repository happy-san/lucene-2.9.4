/* Generated By:JavaCC: Do not edit this line. HTMLParser.java */
package org.apache.lucene.demo.html;

import java.io.*;
import java.util.Properties;

public class HTMLParser implements HTMLParserConstants {
  public static int SUMMARY_LENGTH = 200;

  StringBuffer title = new StringBuffer(SUMMARY_LENGTH);
  StringBuffer summary = new StringBuffer(SUMMARY_LENGTH * 2);
  Properties metaTags=new Properties();
  String currentMetaTag=null;
  String currentMetaContent=null;
  int length = 0;
  boolean titleComplete = false;
  boolean inTitle = false;
  boolean inMetaTag = false;
  boolean inStyle = false;
  boolean afterTag = false;
  boolean afterSpace = false;
  String eol = System.getProperty("line.separator");
  Reader pipeIn = null;
  Writer pipeOut;
  private MyPipedInputStream pipeInStream = null;
  private PipedOutputStream pipeOutStream = null;

  private class MyPipedInputStream extends PipedInputStream{

    public MyPipedInputStream(){
      super();
    }

    public MyPipedInputStream(PipedOutputStream src) throws IOException{
      super(src);
    }

    public boolean full() throws IOException{
      return this.available() >= PipedInputStream.PIPE_SIZE;
    }
  }

  /**
   * @deprecated Use HTMLParser(FileInputStream) instead
   */
  public HTMLParser(File file) throws FileNotFoundException {
    this(new FileInputStream(file));
  }

  public String getTitle() throws IOException, InterruptedException {
    if (pipeIn == null)
      getReader();                                // spawn parsing thread
    while (true) {
      synchronized(this) {
        if (titleComplete || pipeInStream.full())
          break;
        wait(10);
      }
    }
    return title.toString().trim();
  }

  public Properties getMetaTags() throws IOException,
InterruptedException {
    if (pipeIn == null)
      getReader();                                // spawn parsing thread
    while (true) {
      synchronized(this) {
        if (titleComplete || pipeInStream.full())
          break;
        wait(10);
      }
    }
    return metaTags;
  }


  public String getSummary() throws IOException, InterruptedException {
    if (pipeIn == null)
      getReader();                                // spawn parsing thread
    while (true) {
      synchronized(this) {
        if (summary.length() >= SUMMARY_LENGTH || pipeInStream.full())
          break;
        wait(10);
      }
    }
    if (summary.length() > SUMMARY_LENGTH)
      summary.setLength(SUMMARY_LENGTH);

    String sum = summary.toString().trim();
    String tit = getTitle();
    if (sum.startsWith(tit) || sum.equals(""))
      return tit;
    else
      return sum;
  }

  public Reader getReader() throws IOException {
    if (pipeIn == null) {
      pipeInStream = new MyPipedInputStream();
      pipeOutStream = new PipedOutputStream(pipeInStream);
      pipeIn = new InputStreamReader(pipeInStream, "UTF-16BE");
      pipeOut = new OutputStreamWriter(pipeOutStream, "UTF-16BE");

      Thread thread = new ParserThread(this);
      thread.start();                             // start parsing
    }

    return pipeIn;
  }

  void addToSummary(String text) {
    if (summary.length() < SUMMARY_LENGTH) {
      summary.append(text);
      if (summary.length() >= SUMMARY_LENGTH) {
        synchronized(this) {
          notifyAll();
        }
      }
    }
  }

  void addText(String text) throws IOException {
    if (inStyle)
      return;
    if (inTitle)
      title.append(text);
    else {
      addToSummary(text);
      if (!titleComplete && !(title.length() == 0)) {  // finished title
        synchronized(this) {
          titleComplete = true;                   // tell waiting threads
          notifyAll();
        }
      }
    }

    length += text.length();
    pipeOut.write(text);

    afterSpace = false;
  }

  void addMetaTag() {
      metaTags.setProperty(currentMetaTag, currentMetaContent);
      currentMetaTag = null;
      currentMetaContent = null;
      return;
  }

  void addSpace() throws IOException {
    if (!afterSpace) {
      if (inTitle)
        title.append(" ");
      else
        addToSummary(" ");

      String space = afterTag ? eol : " ";
      length += space.length();
      pipeOut.write(space);
      afterSpace = true;
    }
  }

  final public void HTMLDocument() throws ParseException, IOException {
  Token t;
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ScriptStart:
      case TagName:
      case DeclName:
      case Comment1:
      case Comment2:
      case Word:
      case Entity:
      case Space:
      case Punct:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case TagName:
        Tag();
                      afterTag = true;
        break;
      case DeclName:
        t = Decl();
                      afterTag = true;
        break;
      case Comment1:
      case Comment2:
        CommentTag();
                      afterTag = true;
        break;
      case ScriptStart:
        ScriptTag();
                     afterTag = true;
        break;
      case Word:
        t = jj_consume_token(Word);
                      addText(t.image); afterTag = false;
        break;
      case Entity:
        t = jj_consume_token(Entity);
                      addText(Entities.decode(t.image)); afterTag = false;
        break;
      case Punct:
        t = jj_consume_token(Punct);
                      addText(t.image); afterTag = false;
        break;
      case Space:
        jj_consume_token(Space);
                      addSpace(); afterTag = false;
        break;
      default:
        jj_la1[1] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    jj_consume_token(0);
  }

  final public void Tag() throws ParseException, IOException {
  Token t1, t2;
  boolean inImg = false;
    t1 = jj_consume_token(TagName);
   String tagName = t1.image.toLowerCase();
   if(Tags.WS_ELEMS.contains(tagName) ) {
      addSpace();
    }
    inTitle = tagName.equalsIgnoreCase("<title"); // keep track if in <TITLE>
    inMetaTag = tagName.equalsIgnoreCase("<META"); // keep track if in <META>
    inStyle = tagName.equalsIgnoreCase("<STYLE"); // keep track if in <STYLE>
    inImg = tagName.equalsIgnoreCase("<img");     // keep track if in <IMG>

    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ArgName:
        ;
        break;
      default:
        jj_la1[2] = jj_gen;
        break label_2;
      }
      t1 = jj_consume_token(ArgName);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ArgEquals:
        jj_consume_token(ArgEquals);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case ArgValue:
        case ArgQuote1:
        case ArgQuote2:
          t2 = ArgValue();
       if (inImg && t1.image.equalsIgnoreCase("alt") && t2 != null)
         addText("[" + t2.image + "]");

        if(inMetaTag &&
                        (  t1.image.equalsIgnoreCase("name") ||
                           t1.image.equalsIgnoreCase("HTTP-EQUIV")
                        )
           && t2 != null)
        {
                currentMetaTag=t2.image.toLowerCase();
                if(currentMetaTag != null && currentMetaContent != null) {
                addMetaTag();
                }
        }
        if(inMetaTag && t1.image.equalsIgnoreCase("content") && t2 !=
null)
        {
                currentMetaContent=t2.image.toLowerCase();
                if(currentMetaTag != null && currentMetaContent != null) {
                addMetaTag();
                }
        }
          break;
        default:
          jj_la1[3] = jj_gen;
          ;
        }
        break;
      default:
        jj_la1[4] = jj_gen;
        ;
      }
    }
    jj_consume_token(TagEnd);
  }

  final public Token ArgValue() throws ParseException {
  Token t = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ArgValue:
      t = jj_consume_token(ArgValue);
                                              {if (true) return t;}
      break;
    default:
      jj_la1[5] = jj_gen;
      if (jj_2_1(2)) {
        jj_consume_token(ArgQuote1);
        jj_consume_token(CloseQuote1);
                                              {if (true) return t;}
      } else {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case ArgQuote1:
          jj_consume_token(ArgQuote1);
          t = jj_consume_token(Quote1Text);
          jj_consume_token(CloseQuote1);
                                              {if (true) return t;}
          break;
        default:
          jj_la1[6] = jj_gen;
          if (jj_2_2(2)) {
            jj_consume_token(ArgQuote2);
            jj_consume_token(CloseQuote2);
                                              {if (true) return t;}
          } else {
            switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
            case ArgQuote2:
              jj_consume_token(ArgQuote2);
              t = jj_consume_token(Quote2Text);
              jj_consume_token(CloseQuote2);
                                              {if (true) return t;}
              break;
            default:
              jj_la1[7] = jj_gen;
              jj_consume_token(-1);
              throw new ParseException();
            }
          }
        }
      }
    }
    throw new Error("Missing return statement in function");
  }

  final public Token Decl() throws ParseException {
  Token t;
    t = jj_consume_token(DeclName);
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ArgName:
      case ArgEquals:
      case ArgValue:
      case ArgQuote1:
      case ArgQuote2:
        ;
        break;
      default:
        jj_la1[8] = jj_gen;
        break label_3;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ArgName:
        jj_consume_token(ArgName);
        break;
      case ArgValue:
      case ArgQuote1:
      case ArgQuote2:
        ArgValue();
        break;
      case ArgEquals:
        jj_consume_token(ArgEquals);
        break;
      default:
        jj_la1[9] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    jj_consume_token(TagEnd);
    {if (true) return t;}
    throw new Error("Missing return statement in function");
  }

  final public void CommentTag() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case Comment1:
      jj_consume_token(Comment1);
      label_4:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case CommentText1:
          ;
          break;
        default:
          jj_la1[10] = jj_gen;
          break label_4;
        }
        jj_consume_token(CommentText1);
      }
      jj_consume_token(CommentEnd1);
      break;
    case Comment2:
      jj_consume_token(Comment2);
      label_5:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case CommentText2:
          ;
          break;
        default:
          jj_la1[11] = jj_gen;
          break label_5;
        }
        jj_consume_token(CommentText2);
      }
      jj_consume_token(CommentEnd2);
      break;
    default:
      jj_la1[12] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void ScriptTag() throws ParseException {
    jj_consume_token(ScriptStart);
    label_6:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ScriptText:
        ;
        break;
      default:
        jj_la1[13] = jj_gen;
        break label_6;
      }
      jj_consume_token(ScriptText);
    }
    jj_consume_token(ScriptEnd);
  }

  final private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  final private boolean jj_2_2(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_2(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  final private boolean jj_3_1() {
    if (jj_scan_token(ArgQuote1)) return true;
    if (jj_scan_token(CloseQuote1)) return true;
    return false;
  }

  final private boolean jj_3_2() {
    if (jj_scan_token(ArgQuote2)) return true;
    if (jj_scan_token(CloseQuote2)) return true;
    return false;
  }

  public HTMLParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  public boolean lookingAhead = false;
  private boolean jj_semLA;
  private int jj_gen;
  final private int[] jj_la1 = new int[14];
  static private int[] jj_la1_0;
  static {
      jj_la1_0();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x2c7e,0x2c7e,0x10000,0x380000,0x20000,0x80000,0x100000,0x200000,0x3b0000,0x3b0000,0x8000000,0x20000000,0x30,0x4000,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[2];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  public HTMLParser(java.io.InputStream stream) {
     this(stream, null);
  }
  public HTMLParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new HTMLParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public HTMLParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new HTMLParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public HTMLParser(HTMLParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(HTMLParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  final private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = lookingAhead ? jj_scanpos : token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector jj_expentries = new java.util.Vector();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      boolean exists = false;
      for (java.util.Enumeration e = jj_expentries.elements(); e.hasMoreElements();) {
        int[] oldentry = (int[])(e.nextElement());
        if (oldentry.length == jj_expentry.length) {
          exists = true;
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              exists = false;
              break;
            }
          }
          if (exists) break;
        }
      }
      if (!exists) jj_expentries.addElement(jj_expentry);
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[31];
    for (int i = 0; i < 31; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 14; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 31; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

  final private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 2; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  final private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

//    void handleException(Exception e) {
//      System.out.println(e.toString());  // print the error message
//      System.out.println("Skipping...");
//      Token t;
//      do {
//        t = getNextToken();
//      } while (t.kind != TagEnd);
//    }
}
