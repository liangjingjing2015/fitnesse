// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.Symbol;

public class SymbolicPage extends BaseWikitextPage {

  public static final String PROPERTY_NAME = "SymbolicLinks";

  private final WikiPage realPage;

  public SymbolicPage(String name, WikiPage realPage, WikiPage parent) {
    super(name, parent);
    this.realPage = realPage;
    // Perform a cyclic dependency check
  }

  public WikiPage getRealPage() {
    return realPage;
  }

  private boolean containsWikitext() {
    return containsWikitext(realPage);
  }

  @Override
  public WikiPage addChildPage(String name) {
    return realPage.addChildPage(name);
  }

  @Override
  public boolean hasChildPage(String name) {
    return realPage.hasChildPage(name);
  }

  @Override
  public WikiPage getChildPage(String name) {
    WikiPage childPage = realPage.getChildPage(name);
    if (childPage != null) {
      childPage = new SymbolicPage(name, childPage, this);
    }

    return childPage;
  }

  @Override
  public void removeChildPage(String name) {
    realPage.removeChildPage(name);
  }

  @Override
  public List<WikiPage> getChildren() {
    List<WikiPage> children = realPage.getChildren();
    List<WikiPage> symChildren = new LinkedList<WikiPage>();
    //TODO: -AcD- we need a better cyclic infinite recursion algorithm here.
    for (WikiPage child : children) {
      symChildren.add(new SymbolicPage(child.getName(), child, this));
    }
    return symChildren;
  }

  @Override
  public PageData getData() {
    return realPage.getData();
  }

  @Override
  public Collection<VersionInfo> getVersions() {
    return realPage.getVersions();
  }

  @Override
  public WikiPage getVersion(String versionName) {
    return new SymbolicPage(this.getName(), realPage.getVersion(versionName), this.getParent());
  }

  @Override
  public VersionInfo commit(PageData data) {
    return realPage.commit(data);
  }

  @Override
  public String getVariable(String name) {
    if (containsWikitext()) {
      return super.getVariable(name);
    }
    String value = realPage.getVariable(name);
    return (value == null && !isRoot()) ? getParent().getVariable(name) : value;
  }

  @Override
  public String getHtml() {
    if (containsWikitext()) {
      return super.getHtml();
    }
    return realPage.getHtml();
  }

  @Override
  public ParsingPage getParsingPage() {
    if (containsWikitext()) {
      return super.getParsingPage();
    }
    return null;
  }

  @Override
  public Symbol getSyntaxTree() {
    if (containsWikitext()) {
      return super.getSyntaxTree();
    }
    return Symbol.emptySymbol;
  }

  public static boolean containsWikitext(WikiPage wikiPage) {
    if (wikiPage instanceof SymbolicPage) {
      return containsWikitext(((SymbolicPage) wikiPage).realPage);
    } else {
      return wikiPage instanceof WikitextPage;
    }
  }

}
