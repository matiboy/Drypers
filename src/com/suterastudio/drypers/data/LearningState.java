package com.suterastudio.drypers.data;

import java.io.File;

import com.suterastudio.drypers.R;

public class LearningState {
	// fields
	private int letterResource;
	private int objectResource;
	private int writingResource;
	private File file;

	// constructor
	private LearningState(int letterResource, int objectResource,
			int writingResource, File file) {
		super();
		this.letterResource = letterResource;
		this.objectResource = objectResource;
		this.writingResource = writingResource;
		this.file=file;

	}

	// properties
	public int getLetterResource() {
		return letterResource;
	}

	public int getObjectResource() {
		return objectResource;
	}

	public int getWritingResource() {
		return writingResource;
	}

	public File getFile() {
		return file;
	}

	// private static fields
	private static LearningState[] bahasaMalaysiaStates;
	private static LearningState[] englishStates;
	private static int currentIndex;
	private static LearningState[] currentLearningStates;

	// private static methods
	private static boolean isEnglishInitialized() {
		return englishStates != null;
	}

	private static boolean isBahasaInitialized() {
		return bahasaMalaysiaStates != null;
	}

	// initialize English states
	private static void initializeEnglishStates() {
		if (!isEnglishInitialized())
			englishStates = new LearningState[] {
					new LearningState(R.drawable.__learning_a,
							R.drawable.__learning_apple,
							R.drawable.__learning_a_english, DrypersResources.Learning_A_English),
					new LearningState(R.drawable.__learning_b,
							R.drawable.__learning_ball,
							R.drawable.__learning_b_english, DrypersResources.Learning_B_English),
					new LearningState(R.drawable.__learning_c,
							R.drawable.__learning_cat,
							R.drawable.__learning_c_english, DrypersResources.Learning_C_English),
					new LearningState(R.drawable.__learning_d,
							R.drawable.__learning_duck,
							R.drawable.__learning_d_english, DrypersResources.Learning_D_English),
					new LearningState(R.drawable.__learning_e,
							R.drawable.__learning_egg,
							R.drawable.__learning_e_english, DrypersResources.Learning_E_English) };
	}

	// initialize Bahasa states
	private static void initializeBahasaStates() {
		if (!isBahasaInitialized())
			bahasaMalaysiaStates = new LearningState[] { new LearningState(
					R.drawable.__learning_a, R.drawable.__learning_ayam,
					R.drawable.__learning_a_bahasa, DrypersResources.Learning_A_Bahasa),
					new LearningState(R.drawable.__learning_b,
							R.drawable.__learning_bola,
							R.drawable.__learning_b_bahasa, DrypersResources.Learning_B_Bahasa),
					new LearningState(R.drawable.__learning_c,
							R.drawable.__learning_cawan,
							R.drawable.__learning_c_bahasa, DrypersResources.Learning_C_Bahasa),
					new LearningState(R.drawable.__learning_d,
							R.drawable.__learning_datuk,
							R.drawable.__learning_d_bahasa, DrypersResources.Learning_D_Bahasa),
					new LearningState(R.drawable.__learning_e,
							R.drawable.__learning_epal,
							R.drawable.__learning_e_bahasa, DrypersResources.Learning_E_Bahasa) };
	}

	// public static methods
	public static void initializeState(LanguagePreference language) {
		currentIndex = 0;
		if (language == LanguagePreference.BAHASA_MALAYSIA) {
			initializeBahasaStates();
			currentLearningStates = bahasaMalaysiaStates;
		} else if (language == LanguagePreference.ENGLISH) {
			initializeEnglishStates();
			currentLearningStates = englishStates;
		}
	}

	public static boolean isFirstState() {
		return currentIndex == 0;
	}

	public static boolean isLastState() {
		return currentIndex == currentLearningStates.length - 1;
	}

	public static LearningState getCurrentstate() {
		return currentLearningStates[currentIndex];
	}

	public static void goToNextState() {
		if (!isLastState())
			currentIndex++;
	}

	public static void goToPreviousState() {
		if (!isFirstState())
			currentIndex--;
	}
	@Override
	public boolean equals(Object o) {
		if (o.getClass()==this.getClass())
			return ((LearningState) o).file.getAbsolutePath().equals(this.file.getAbsolutePath());
		return super.equals(o);
	}
	
	public int getIndex()
	{
		switch(this.letterResource)
		{
		case R.drawable.__learning_a:
			return 0;
		case R.drawable.__learning_b:
			return 1;
		case R.drawable.__learning_c:
			return 2;
		case R.drawable.__learning_d:
			return 3;
		case R.drawable.__learning_e:
			return 4;
		default:
			return -1;
		}
	}

	public CharSequence getBabbleName() {
		switch(this.objectResource)
		{
		case R.drawable.__learning_ayam:
			return "My Ayam Babble";
		case R.drawable.__learning_bola:
			return "My Bola Babble";
		case R.drawable.__learning_cawan:
			return "My Cawan Babble";
		case R.drawable.__learning_datuk:
			return "My Datuk Babble";
		case R.drawable.__learning_epal:
			return "My Epal Babble";
		case R.drawable.__learning_apple:
			return "My Apple Babble";
		case R.drawable.__learning_ball:
			return "My Ball Babble";
		case R.drawable.__learning_cat:
			return "My Cat Babble";
		case R.drawable.__learning_duck:
			return "My Duck Babble";
		case R.drawable.__learning_egg:
			return "My Egg Babble";
		default:
			return "New Babble";
		}
	}
}
