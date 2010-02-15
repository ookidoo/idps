package iDPS.model;

import iDPS.Attributes;
import iDPS.Launcher;
import iDPS.Race;
import iDPS.Talents;
import iDPS.gear.Setup;
import iDPS.gear.Weapon;
import iDPS.gear.Weapon.weaponType;

public abstract class Calculations {
	
	public enum ModelType { Combat, Mutilate };
	protected Attributes attrTotal;
	
	protected float avgCpFin, rupPerCycle;
	float ppsIP1, ppsIP2;
	float dpsWH, dpsDP, dpsIP, dpsRU, total;
	protected float envenomUptime;
	float epAGI, epHIT, epCRI, epHST, epARP, epEXP;
	protected Setup setup;
	protected float mhSPS, ohSPS, mhSCPS, ohSCPS;
	protected float mhWPS, ohWPS, mhWCPS, ohWCPS;
	protected Modifiers mod;
	protected Talents talents;
	
	protected float bbIncrease;
	
	protected float totalATP;
	
	protected float fightDuration = 300;
	
	protected ModelType type;
	
	private void reset() {
		mhSPS = 0;
		mhSCPS = 0;
		ohSPS = 0;
		ohSCPS = 0;
		mhWPS = 0;
		mhWCPS = 0;
		ohWPS = 0;
		ohWCPS = 0;
	}
	
	protected abstract void calcCycle();
		
	protected float calcDeadlyPoisonDPS() {
		//System.out.println("dp ap: "+totalATP);
		float dps = (296+0.108F*totalATP)/12*5 * (1+talents.getVilePoisons());
		// Global Mods
		dps *= talents.getMurder() * talents.getHfb() * 1.03F * 1.13F * 0.971875F;
		if (talents.getKs())
			dps *= 1 + (0.2F * 2.5F/75F);
		return dps;
	}
	
	protected abstract void calcDPS();
	
	protected float calcEnvenomDamage() {
		float dmg = (215*avgCpFin + 0.09F*avgCpFin * totalATP)*(1+talents.getVilePoisons()+talents.getFindWeakness());
		dmg += dmg*(mod.getPhysCritMult()-1)*mod.getHtFin().crit;
		// Global Mods
		dmg *= talents.getMurder() * talents.getHfb() * 1.03F * 1.13F;
		return dmg;
	}
	
	public void calcEP() {
		float dpsATP;
		Calculations c;
		try {
			//System.out.println("EP Calcs");
			c = getClass().newInstance();
			Attributes attr = new Attributes();
			//System.out.println("EP ATP");
			attr.setAtp(1);
			c.calculate(attr, setup);
			dpsATP = c.total - total;
			attr.clear();
			//System.out.println("EP AGI");
			attr.setAgi(1);
			c.calculate(attr, setup);
			epAGI = (c.total - total) / dpsATP;
			attr.clear();
			//System.out.println("EP HIT");
			attr.setHit(1);
			c.calculate(attr, setup);
			epHIT = (c.total - total) / dpsATP;
			attr.clear();
			//System.out.println("EP CRI");
			attr.setCri(1);
			c.calculate(attr, setup);
			epCRI = (c.total - total) / dpsATP;
			attr.clear();
			//System.out.println("EP HST");
			attr.setHst(1);
			c.calculate(attr, setup);
			epHST = (c.total - total) / dpsATP;
			attr.clear();
			//System.out.println("EP EXP");
			attr.setExp(1);
			c.calculate(attr, setup);
			epEXP = (c.total - total) / dpsATP;
			attr.clear();
			//System.out.println("EP ARP");
			attr.setArp(1);
			c.calculate(attr, setup);
			epARP = (c.total - total) / dpsATP;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected float calcEviscerateDamage() {
		float baseDmg = 0;
		if (avgCpFin > 4)
			baseDmg = 1607*(5-avgCpFin)+1977*(avgCpFin-4);
		float dmg = (baseDmg + 0.07F*avgCpFin * totalATP)*(1+0.2F+0.15F);
		dmg += dmg*(mod.getPhysCritMult()-1)*mod.getHtFin().crit;
		// Global Mods
		dmg *= 1.04F * 1.03F;
		
		dmg *= mod.getModArmorMH();
		dmg *= 1+bbIncrease;
		return dmg;
	}
	
	protected float calcInstantPoisonDPS() {
		// possible procs per sec
		float ip_ppps = 0, dp_ppps = 0;
		Weapon wip;
		if (setup.getWeapon2().getSpeed() >= setup.getWeapon1().getSpeed()) {
			wip = setup.getWeapon2();
			ip_ppps = ohWPS + ohSPS;
			dp_ppps = mhWPS + mhSPS;
		} else {
			wip = setup.getWeapon1();
			ip_ppps = mhWPS + mhSPS;
			dp_ppps = ohWPS + ohSPS;
		}
		float ipProcChance, dpProcChance, hitChance, procsPerSec, damage;
		ipProcChance  = wip.getSpeed()/1.4F*(0.2F+0.02F*talents.getImprovedPoisons());
		ipProcChance *= 1+(envenomUptime*0.75F);
		//System.out.println("avg IP proc chance: "+ipProcChance);
		dpProcChance  = 0.3F + 0.04F*talents.getImprovedPoisons();
		dpProcChance += envenomUptime*0.15F;
		hitChance = Math.min(1, 0.83F+mod.getSpellHitPercent()/100);
		// Primary Procs
		ppsIP1  = ip_ppps*ipProcChance*hitChance;
		// Secondary Procs
		ppsIP2 = dp_ppps*dpProcChance*hitChance;
		procsPerSec = ppsIP1+ppsIP2;
		//System.out.println("IP pps: "+procsPerSec);
		// Damage
		damage = (350+0.09F*totalATP) * (1+talents.getVilePoisons()) * 0.971875F;
		//System.out.println("IP Proc dmg: "+damage);
		damage += damage*(mod.getPoisonCritMult()-1)*(mod.getSpellCritPercent()/100F);
		// Global Mods
		damage *= talents.getMurder() * talents.getHfb() * 1.03F * 1.13F;
		if (talents.getKs())
			damage *= 1 + (0.2F * 2.5F/75F);
		return procsPerSec * damage;
	}
	
	/*protected void calcMongoose() {
		float attacksPerSec, uptimeMH = 0, uptimeOH = 0;
		if (setup.isEnchanted(16) && setup.getEnchant(16).getId()==2673) {
			attacksPerSec = mhWPS + mhSPS;
			uptimeMH = setup.getWeapon1().getPPMUptime(1, 15, attacksPerSec);
		}
		if (setup.isEnchanted(17) && setup.getEnchant(17).getId()==2673) {
			attacksPerSec = ohWPS + ohSPS;
			uptimeOH = setup.getWeapon2().getPPMUptime(1, 15, attacksPerSec);
		}
		if ((uptimeMH+uptimeOH)>0) {
		// mongoose ~73 cri & 132 ap
			float uptimeD = uptimeMH * uptimeOH;
			float uptimeS = (1-(1-uptimeMH)*(1-uptimeOH))-uptimeD;
			mod.registerPhysCritProc(146, uptimeD);
			mod.registerPhysCritProc(73, uptimeS);
			mod.registerStaticHasteProc(0.02F, uptimeMH);
			mod.registerStaticHasteProc(0.02F, uptimeOH);
			totalATP += 264*uptimeD;
			totalATP += 132*uptimeS;
		}
	}*/
	
	protected void calcProcs() {
		float hitsPerSec = mhWPS+ohWPS+mhSPS+ohSPS;
		float critsPerSec = mhWCPS+ohWCPS+mhSCPS+ohSCPS;
		Attributes a;
		
		// Bloodlust / Heroism
		float uptime = 40/fightDuration;
		mod.registerStaticHasteProc(0.3F, uptime);
		
		// Orc Racial
		if (setup.getRace().getType() == Race.Type.Orc) {
			a = new Attributes(Attributes.Type.ATP, 322);
			mod.registerProc(new ProcStatic(a, 15, 120, 1, 1));
		}
		
		// Troll Racial
		if (setup.getRace().getType() == Race.Type.Troll) {
			uptime = getMaxUses(180)*10F/fightDuration;
			mod.registerStaticHasteProc(0.2F, uptime);
		}
		
		// Blade Flurry
		if (talents.getBf()) {
			uptime = getMaxUses(120)*15F/fightDuration;
			mod.registerStaticHasteProc(0.2F, uptime);
		}
		
		// Mongoose
		a = new Attributes(Attributes.Type.AGI, 120);
		if (setup.getWeapon1() != null && setup.isEnchanted(16) && setup.getEnchant(16).getId()==2673) {
			Proc p = new ProcPerMinute(a, 15, 0, 1, 
					setup.getWeapon1(), (mhWPS+mhSPS),
					setup.getWeapon2(), 0);
			mod.registerProc(p);
			mod.registerStaticHasteProc(0.02F, p.getUptime());
		}
		if (setup.getWeapon2() != null && setup.isEnchanted(17) && setup.getEnchant(17).getId()==2673) {
			Proc p = new ProcPerMinute(a, 15, 0, 1, 
					setup.getWeapon1(), 0,
					setup.getWeapon2(), (ohWPS+ohSPS));
			mod.registerProc(p);
			mod.registerStaticHasteProc(0.02F, p.getUptime());
		}
		
		// Berserking
		a = new Attributes(Attributes.Type.ATP, 400);
		if (setup.getWeapon1() != null && setup.isEnchanted(16) && setup.getEnchant(16).getId()==3789) {
			Proc p = new ProcPerMinute(a, 15, 0, 1, 
					setup.getWeapon1(), (mhWPS+mhSPS),
					setup.getWeapon2(), 0);
			mod.registerProc(p);
		}
		if (setup.getWeapon2() != null && setup.isEnchanted(17) && setup.getEnchant(17).getId()==3789) {
			Proc p = new ProcPerMinute(a, 15, 0, 1, 
					setup.getWeapon1(), 0,
					setup.getWeapon2(), (ohWPS+ohSPS));
			mod.registerProc(p);
		}
		
		// Hyperspeed Accelerators
		if (setup.isEnchanted(8) && setup.getEnchant(8).getId()==3604) {
			a = new Attributes(Attributes.Type.HST, 340);
			mod.registerProc(new ProcStatic(a, 12, 60, 1, 1));
		}
		
		// Swordguard Embroidery
		if (setup.isEnchanted(3) && setup.getEnchant(3).getId()==3730) {
			a = new Attributes(Attributes.Type.ATP, 400);
			mod.registerProc(new ProcStatic(a, 15, 60, 0.25F, hitsPerSec));
		}
		
		// Grim Toll
		if (setup.contains(40256)>0) {
			a = new Attributes(Attributes.Type.ARP, 612);
			mod.registerProc(new ProcStatic(a, 10, 45, 0.15F, hitsPerSec));
		}
		
		// Mirror of Truth
		if (setup.contains(40684)>0) {
			a = new Attributes(Attributes.Type.ATP, 1000);
			mod.registerProc(new ProcStatic(a, 10, 45, 0.1F, critsPerSec));
		}
		
		// Tears of Bitter Anguish
		if (setup.contains(43573)>0) {
			a = new Attributes(Attributes.Type.HST, 410);
			mod.registerProc(new ProcStatic(a, 10, 45, 0.1F, critsPerSec));
		}
		
		// Darkmoon Card: Greatness
		if (setup.contains(44253)>0) {
			a = new Attributes(Attributes.Type.AGI, 300);
			mod.registerProc(new ProcStatic(a, 15, 45, 0.35F, hitsPerSec));
		}
		
		// Pyrite Infuser
		if (setup.contains(45286)>0) {
			a = new Attributes(Attributes.Type.ATP, 1234);
			mod.registerProc(new ProcStatic(a, 10, 45, 0.1F, critsPerSec));
		}
		
		// Blood of the Old God
		if (setup.contains(45522)>0) {
			a = new Attributes(Attributes.Type.ATP, 1284);
			Proc p = new ProcStatic(a, 10, 45, 0.1F, critsPerSec);
			mod.registerProc(p);
		}
		
		// Comet's Trail
		if (setup.contains(45609)>0) {
			a = new Attributes(Attributes.Type.HST, 726);
			mod.registerProc(new ProcStatic(a, 10, 45, 0.15F, hitsPerSec));
		}
		
		// Mjolnir Runestone
		if (setup.contains(45931)>0) {
			a = new Attributes(Attributes.Type.ARP, 665);
			mod.registerProc(new ProcStatic(a, 10, 45, 0.15F, hitsPerSec));
		}
		
		// Dark Matter
		if (setup.contains(46038)>0) {
			a = new Attributes(Attributes.Type.CRI, 612);
			mod.registerProc(new ProcStatic(a, 10, 45, 0.15F, hitsPerSec));
		}
		
		// Banner of Victory
		if (setup.contains(47214)>0) {
			a = new Attributes(Attributes.Type.ATP, 1008);
			mod.registerProc(new ProcStatic(a, 10, 45, 0.2F, hitsPerSec));
		}
		
		// Death's Choice
		if (setup.containsAny(47303,47115)) {
			a = new Attributes(Attributes.Type.AGI, 450);
			mod.registerProc(new ProcStatic(a, 15, 45, 0.35F, hitsPerSec));
		}
		
		// Death's Choice Heroic
		if (setup.containsAny(47464,47131)) {
			a = new Attributes(Attributes.Type.AGI, 510);
			Proc p = new ProcStatic(a, 15, 45, 0.35F, hitsPerSec);
			mod.registerProc(p);
		}
		
		// Mark of Supremacy
		if (setup.contains(47734)>0) {
			a = new Attributes(Attributes.Type.ATP, 1024);
			mod.registerProc(new ProcStatic(a, 20, 120, 1, 1));
		}
		
		// Vengeance of the Forsaken
		if (setup.containsAny(47881,47725)) {
			float timeToCap = 5/hitsPerSec;
			float avgAtp = (timeToCap*537.5F + (20-timeToCap)*1075F)/20F;
			a = new Attributes(Attributes.Type.ATP, avgAtp);
			mod.registerProc(new ProcStatic(a, 20, 120, 1, 1));
		}
		
		// Vengeance of the Forsaken
		if (setup.containsAny(48020,47948)) {
			float timeToCap = 5/hitsPerSec;
			float avgAtp = (timeToCap*625F + (20-timeToCap)*1250F)/20F;
			a = new Attributes(Attributes.Type.ATP, avgAtp);
			mod.registerProc(new ProcStatic(a, 20, 120, 1, 1));
		}
		
		// Shard of the Crystal Heart
		if (setup.contains(48722)>0) {
			a = new Attributes(Attributes.Type.HST, 512);
			mod.registerProc(new ProcStatic(a, 20, 120, 1, 1));
		}
		
		// Black Bruise
		if (setup.containsAny(50035,50692)) {
			float bbUptime = calcDWPPMUptime(0.3F, 10);
			//System.out.println("BB Uptime: "+bbUptime);
			if(setup.containsAny(50692))
				bbIncrease = bbUptime*0.10F;
			else
				bbIncrease =bbUptime*0.09F;
		} else
			bbIncrease = 0;
		
		// Needle-Encrusted Scorpion
		if (setup.contains(50198)>0) {
			a = new Attributes(Attributes.Type.ARP, 678);
			mod.registerProc(new ProcStatic(a, 10, 50, 0.1F, critsPerSec));
		}
		
		// Whispering Fanged Skull
		if (setup.contains(50342)>0) {
			a = new Attributes(Attributes.Type.ATP, 1100);
			mod.registerProc(new ProcStatic(a, 15, 45, 0.35F, hitsPerSec));
		}
		
		// Whispering Fanged Skull Heroic
		if (setup.contains(50343)>0) {
			a = new Attributes(Attributes.Type.ATP, 1250);
			mod.registerProc(new ProcStatic(a, 15, 45, 0.35F, hitsPerSec));
		}
		
		// Herkuml War Token
		if (setup.contains(50355)>0) {
			a = new Attributes(Attributes.Type.ATP, 340);
			mod.registerProc(new ProcStatic(a, 1, 1, 1, 0)); // uptime = 100%
		}
		
		// Deathbringer's Will
		if (setup.contains(50362)>0) {
			a = new Attributes(Attributes.Type.ATP, 1200);
			mod.registerProc(new ProcStatic(a, 30, 315, 0.5F, hitsPerSec));
			a = new Attributes(Attributes.Type.AGI, 600);
			mod.registerProc(new ProcStatic(a, 30, 315, 0.5F, hitsPerSec));
			a = new Attributes(Attributes.Type.HST, 600);
			mod.registerProc(new ProcStatic(a, 30, 315, 0.5F, hitsPerSec));
		}
		
		// Deathbringer's Will Heroic
		if (setup.contains(50363)>0) {
			a = new Attributes(Attributes.Type.ATP, 1400);
			mod.registerProc(new ProcStatic(a, 30, 315, 0.5F, hitsPerSec));
			a = new Attributes(Attributes.Type.AGI, 700);
			mod.registerProc(new ProcStatic(a, 30, 315, 0.5F, hitsPerSec));
			a = new Attributes(Attributes.Type.HST, 700);
			mod.registerProc(new ProcStatic(a, 30, 315, 0.5F, hitsPerSec));
		}
		
		// Ashen Band of ...
		if (setup.containsAny(50401,50402)) {
			a = new Attributes(Attributes.Type.ATP, 480);
			mod.registerProc(new ProcPerMinute(a, 10, 60, 1,
					setup.getWeapon1(), (mhWPS+mhSPS),
					setup.getWeapon2(), (ohWPS+ohSPS)));
		}
	}
	
	protected float calcDWPPMUptime(float ppm, float buffLen) {
		float apsMH, apsOH, utMH, utOH, ut;
		
		apsMH = setup.getWeapon1().getEffectiveAPS(mod.getHastePercent()/100)*(mod.getHtMH().getContacts());
		apsMH += mhSPS;
		apsOH = setup.getWeapon2().getEffectiveAPS(mod.getHastePercent()/100)*(mod.getHtOH().getContacts());
		apsOH += ohSPS;
		
		utMH = setup.getWeapon1().getPPMUptime(ppm, buffLen, apsMH);
		utOH = setup.getWeapon1().getPPMUptime(ppm, buffLen, apsOH);
		ut = 1-((1-utMH)*(1-utOH));
		
		return ut;
	}
	
	protected float calcDWUptime(float pProc, float buffLen) {
		float apsMH, apsOH, utMH, utOH, ut;
		
		apsMH = setup.getWeapon1().getEffectiveAPS(mod.getHastePercent()/100)*(mod.getHtMH().getContacts());
		apsMH += mhSPS;
		apsOH = setup.getWeapon2().getEffectiveAPS(mod.getHastePercent()/100)*(mod.getHtOH().getContacts());
		apsOH += ohSPS;
		
		utMH = setup.getWeapon1().getUptime(0.04F, buffLen, apsMH);
		utOH = setup.getWeapon1().getUptime(0.04F, buffLen, apsOH);
		ut = 1-((1-utMH)*(1-utOH));
		
		return ut;
	}
	
	protected float calcHeartpierceRegen() {
		float regen = 0;
		float apsMH, apsOH, pp2s;
		apsMH = setup.getWeapon1().getEffectiveAPS(mod.getHastePercent()/100)*(mod.getHtMH().getContacts());
		apsMH += mhSPS;
		apsOH = setup.getWeapon2().getEffectiveAPS(mod.getHastePercent()/100)*(mod.getHtOH().getContacts());
		apsOH += ohSPS;
		pp2s = setup.getWeapon1().getPPMUptime(1, 2, apsMH);
		pp2s += setup.getWeapon2().getPPMUptime(1, 2, apsOH);
		if (setup.containsAny(49982)) {
			float uptime = calcDWPPMUptime(1, 10);
			regen += uptime*2*(1-pp2s);
		}
		if (setup.containsAny(50641)) {
			float uptime = calcDWPPMUptime(1, 12);
			regen += uptime*2*(1-pp2s);
		}
		
		return regen;
	}
	
	protected float calcRuptureDPS() {
		if (rupPerCycle==0)
			return 0;
		float tick4cp, tick5cp, avgTick;
		tick4cp = 199 + 0.03428571F*totalATP;
		tick5cp = 217 + 0.0375F*totalATP;
		avgTick = tick4cp*(5-avgCpFin)+tick5cp*(avgCpFin-4);
		avgTick *= 1.42F* 1.04F * 1.18F * 1.03F * 1.3F;
		return avgTick/2;
	}
	
	public void calculate(Setup g) {
		calculate(null, g);
	}
	
	public void calculate(Attributes a, Setup g) {
		if (g ==null) {
			System.err.println("Cant calc with no setup!");
			return;
		}
		reset();
		
		talents = g.getTalents();
		
		attrTotal = new Attributes(a);
		setup = g;
		
		attrTotal.add(setup.getAttributes());
		mod = new Modifiers(attrTotal, setup);
		
		//System.out.println(">> Iteration 1");
		calcCycle();
		calcProcs();
		//System.out.println("  AP: "+mod.getTotalATP());
		//System.out.println(">> Iteration 2");
		calcCycle();
		mod.calcMods();
		calcProcs();
		//System.out.println("  AP: "+mod.getTotalATP());
		//System.out.println(">> Iteration 3");
		calcCycle();
		mod.calcMods();
		calcProcs();
		//System.out.println("  AP: "+mod.getTotalATP());
		
		totalATP = mod.getTotalATP();
		//System.out.println("AP: "+totalATP);
		
		calcDPS();
	}
	
	protected float calcERegen() {
		float eRegen = 10;
		
		// Racial
		if (setup.getRace().getType() == Race.Type.BloodElf)
			eRegen += 15F/120F;
		
		// ToTT every 32 sec
		float eLossTOT = 15F/32F;
		if (setup.getTier10()>=2)
			eLossTOT *= -1F;
		eRegen -= eLossTOT;
		
		// Heartpierce
		if (setup.containsAny(49982,50641))
			eRegen += calcHeartpierceRegen();
		
		//System.out.println("total regen: "+eRegen);
		return eRegen;
	}
	
	protected float calcWhiteDPS() {
		HitTable htMH = mod.getHtMH();
		HitTable htOH = mod.getHtMH();
		float dpsMH = 0, dpsOH = 0;
		mhWPS = 0; mhWCPS = 0;
		ohWPS = 0; ohWCPS = 0;
		// Mainhand
		if (setup.getWeapon1() != null) {
			dpsMH += setup.getWeapon1().getDps() + ((float)totalATP/14F);
			dpsMH *= htMH.glance*0.75F + htMH.crit * mod.getPhysCritMult() + htMH.hit;
			dpsMH *= mod.getHastePercent()/100F + 1;
			mhWPS = setup.getWeapon1().getEffectiveAPS(mod.getHastePercent()/100F)*mod.getHtMH().getContacts();
			mhWCPS = setup.getWeapon1().getEffectiveAPS(mod.getHastePercent()/100F)*mod.getHtMH().getCrit();
		}
		// Offhand
		if (setup.getWeapon2() != null) {
			dpsOH += setup.getWeapon2().getDps() + ((float)totalATP/14F) * 0.75F;
			dpsOH *= htOH.glance*0.75F + htOH.crit * mod.getPhysCritMult() + htOH.hit;
			dpsOH *= mod.getHastePercent()/100F + 1;
			ohWPS = setup.getWeapon2().getEffectiveAPS(mod.getHastePercent()/100F)*mod.getHtOH().getContacts();
			ohWCPS = setup.getWeapon2().getEffectiveAPS(mod.getHastePercent()/100F)*mod.getHtOH().getCrit();
		}
		// Sword Spec
		if (talents.getHnS()>0) {
			Weapon w1 = setup.getWeapon1(), w2 = setup.getWeapon2();
			float ssDmg, ssPps = 0, ssDps;
			ssDmg  = setup.getWeapon1().getAverageDmg(totalATP);
			// Mainhand Procs
			if (w1.getType() == weaponType.Axe || w1.getType() == weaponType.Sword) {
				ssPps += w1.getEffectiveAPS(mod.getHastePercent()/100F)*(htMH.getContacts())*(talents.getHnS()/100F);
				ssPps += mhSPS * (talents.getHnS()/100F);
			}
			// Offhand Procs
			if (w2.getType() == weaponType.Axe || w2.getType() == weaponType.Sword) {
				ssPps += setup.getWeapon2().getEffectiveAPS(mod.getHastePercent()/100F)*(htOH.getContacts())*(talents.getHnS()/100F);
				ssPps += ohSPS * (talents.getHnS()/100F);
			}
			mhWPS += ssPps * htMH.getContacts();
			mhWCPS += ssPps * htMH.getCrit();
			ssDps  = ssDmg * ssPps;
			ssDps *= htMH.glance*0.75F + htMH.crit * mod.getPhysCritMult() + htMH.hit;
			dpsMH += ssDps;
		}
		if (setup.containsAny(50351,50706)) {
			float ppsMH, ppsOH, moteFactor, dmgMH, dmgOH;
			if (setup.containsAny(50706))
				moteFactor = 1/7F;
			else
				moteFactor = 1/8F;
			//System.out.println(">>MF: "+moteFactor);
			ppsMH = (setup.getWeapon1().getEffectiveAPS(mod.getHastePercent()/100F)*(htMH.getContacts()) + mhSPS)*0.5F*moteFactor;
			ppsOH = (setup.getWeapon2().getEffectiveAPS(mod.getHastePercent()/100F)*(htOH.getContacts()) + ohSPS)*0.5F*moteFactor;
			//System.out.println(">>Procs: MH: "+ppsMH+ " OH: "+ppsOH);
			
			mhSPS += ppsMH * mod.getHtMHS().getContacts();
			ohSPS += ppsOH * mod.getHtOHS().getContacts();
			mhSCPS += ppsMH * mod.getHtMHS().getCrit();
			ohSCPS += ppsOH * mod.getHtOHS().getCrit();
			
			dmgMH = setup.getWeapon1().getAverageDmg(totalATP)/2F;
			dmgMH = mod.getHtMHS().getHit()*dmgMH + mod.getHtMHS().getCrit()*dmgMH*mod.getPhysCritMult();
			dmgOH = setup.getWeapon2().getAverageDmg(totalATP)/2F;
			dmgOH = mod.getHtOHS().getHit()*dmgOH + mod.getHtOHS().getCrit()*dmgOH*mod.getPhysCritMult();
			//System.out.println(">>Dmg: MH: "+dmgMH+ " OH: "+dmgOH);
			//System.out.println(">>DPS added: "+(ppsMH*dmgMH+ppsOH*dmgOH));
			dpsMH += ppsMH * dmgMH;
			dpsOH += ppsMH * dmgOH;
		}
		// Global Mods
		dpsMH *= 1.04F * talents.getHfb() * 1.03F * 1.04F;
		dpsOH *= 1.04F * talents.getHfb() * 1.03F * 1.04F;
		if (talents.getKs()) {
			dpsMH *= 1 + (0.2F * 2.5F/75F);
			dpsOH *= 1 + (0.2F * 2.5F/75F);
		}
		// Armor Reduction
		dpsMH *= mod.getModArmorMH();
		dpsOH *= mod.getModArmorOH();
		float dps = dpsMH+dpsOH;
		dps *= 1+bbIncrease;
		return dps;
	}
	
	public float getEpAGI() {
		return epAGI;
	}
	
	public float getEpARP() {
		return epARP;
	}
	
	public float getEpCRI() {
		return epCRI;
	}
	
	public float getEpEXP() {
		return epEXP;
	}

	public float getEpHIT() {
		return epHIT;
	}

	public float getEpHST() {
		return epHST;
	}

	public float getMhSPS() {
		return mhSPS;
	}

	public Modifiers getModifiers() {
		return mod;
	}

	public float getOhSPS() {
		return ohSPS;
	}

	public float getTotalDPS() {
		return total;
	}
	
	protected int getMaxUses(int cooldown) {
		return 1 + ((int) fightDuration/cooldown);
	}
	
	public static Calculations createInstance() {
		Setup s = Launcher.getApp().getSetup();
		ModelType m = s.getTalents().getModel();
		switch (m) {
			default:
			case Combat:
				return new CalculationsCombat();
			case Mutilate:
				return new CalculationsMutilate();
		}
	}

}
