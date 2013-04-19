


private boolean graphBuilt = false;

void removeDuplicates () {
	
	final MutableIterator2<SectionInterface> iterator = new MutableIterator2<SectionInterface>();
	iterator.add(Collections.<SectionInterface>unmodifiableCollection( lines ));
	iterator.add(Collections.<SectionInterface>unmodifiableCollection( lines2 ));
	
	// build graph
	for (final SectionInterface section : iterator) {
		section.start().allSections.add(section);
		section.end().allSections.add(section);
	}
	
	graphBuilt = true;
	
//	final Collection<SectionInterface> duplicates = new LinkedList<SectionInterface>();
	final MutableIterator2<Section> iterator2 = new MutableIterator2<Section>(lines2);
	for (final Section section : iterator2) {
		
		for (int i = 0; i < 2; i++) {
			final OsmNode node1 = i == 0 ? section.start() : section.end();
			final OsmNode node2 = i == 0 ? section.end() : section.start();
			
			for (final SectionInterface aSection : node1.allSections) {
				final OsmNode aNode2 = aSection.start() == node1 ? aSection.end() : aSection.start();
				
				if (section == aSection) {
					continue;
				}
				
				if (aNode2.equals(node2)) {
System.out.println(aSection + " " + section);
					iterator2.remove();
					node1.allSections.remove(section);
					node2.allSections.remove(section);
					aNode1.allSections.remove(aSection);
					aNode2.allSections.remove(aSection);
//					duplicates.add(section);
					break;  // limits to 1 dup
				}
			}
		}
		
	}
	
}



// merge sections ... for normalisation, I believe??
void cleanup2 () {
	assert graphBuilt;
	
	final Collection<SectionInterface> allSections = null;
	final Collection<SectionInterface> newSections = new LinkedList<SectionInterface>();
	
	// merge lines
	// MutableIterator doesn't quite fit this use case :(
	boolean changed = true;
	while (changed) {
		changed = false;
		
		final Collection<SectionInterface> toRemove = new LinkedList<SectionInterface>();
		final Collection<SectionInterface> toAdd = new LinkedList<SectionInterface>();
		
		// :BUG: inefficient as hell ... we really need that mutable iterator!
		final Iterator<SectionInterface> i = allSections.iterator();
		while (i.hasNext()) {
			SectionInterface section = i.next();
			
			if (section.end().allSections.size() == 1) {
				// merge
				SectionInterface mergedSection = mergeSections(section, section.end().allSections.iterator().next());
				if (mergedSection != null) {
					toRemove.add(section);
					toRemove.add(section.end().allSections.iterator().next());
					toAdd.add(mergedSection);
					changed = true;
					continue;
				}
			}
			
/*
			// :TODO: order
			if (section.start().allSections.size() == 1) {
				// merge
				SectionInterface mergedSection = mergeSections(section, section.start().allSections.iterator().next());
				if (mergedSection != null) {
					toRemove.add(section);
					toRemove.add(section.end().allSections.iterator().next());
					toAdd.add(mergedSection);
					changed = true;
					continue;
				}
			}
*/
		}
		
		if (changed) {
			allSections.removeAll(toRemove);
			newSections.addAll(toAdd);
		}
	}
}



private SectionInterface mergeSections (final SectionInterface section1, final SectionInterface section2) {
	final String osmHighway = section1.tags().get("highway");
	if (osmHighway != section2.tags().get("highway")) {
		return null;  // conflicting highway types means we should break here
	}
	final String osmRef;
	if (section1.tags().get("ref") != OsmTags.NO_VALUE) {
		osmRef = section1.tags().get("ref");
	}
	else {
		osmRef = section2.tags().get("ref");
		// :BUG: this isn't entirely correct, but should yield good results in most cases
	}
	final LinkedList<OsmNode> combination = new LinkedList<OsmNode>();
	combination.addAll(section1.combination());
	combination.addAll(section2.combination());
	final OsmTags tags = new OsmTags () {
		public String get (final String key) {
			if (key == "highway") {
				return osmHighway;
			}
			if (key == "ref") {
				return osmRef;
			}
			return OsmTags.NO_VALUE;
		}
	};
	return new MergedSection(combination, tags);
}



private static class MergedSection implements SectionInterface {
	
	final private OsmTags tags;
	final private LinkedList<OsmNode> combination;
	
	MergedSection (final LinkedList<OsmNode> combination, final OsmTags tags) {
		this.combination = combination;
		this.tags = tags;
	}
	
	public OsmTags tags () {
		return tags;
	}
	
	public Collection<OsmNode> combination () {
		return combination;
	}
	
	public OsmNode start () {
		return combination.getFirst();
	}
	
	public OsmNode end () {
		return combination.getLast();
	}
	
}
