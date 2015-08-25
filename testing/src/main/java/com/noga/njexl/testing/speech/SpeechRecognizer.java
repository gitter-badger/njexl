package com.noga.njexl.testing.speech;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.result.WordResult;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by noga on 14/08/15.
 */
public class SpeechRecognizer {

    public static final String PATH = "resource:/edu/cmu/sphinx/models/%s/" ;

    public static final String AcousticModelPath = PATH + "%s" ;

    public static final String DictPath = PATH + "cmudict-%s.dict"  ;

    public static final String LanguageModelPath = PATH + "%s.lm.bin"  ;


    Configuration configuration ;

    public SpeechRecognizer(String lang){
        // only lower-case
        lang = lang.toLowerCase() ;
        configuration = new Configuration();
        // Set path to acoustic model.
        configuration.setAcousticModelPath(String.format(AcousticModelPath,lang,lang));
        // Set path to dictionary.
        configuration.setDictionaryPath(String.format(DictPath,lang,lang,lang));
        // Set language model.
        configuration.setLanguageModelPath(String.format(LanguageModelPath,lang,lang,lang));
    }

    public List<String> recognize() throws Exception {
        LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
        // Start recognition process pruning previously cached data.
        recognizer.startRecognition(true);
        System.out.println(">>>>Talk now...");
        SpeechResult result = recognizer.getResult();
        // Pause recognition process. It can be resumed then with startRecognition(false).
        recognizer.stopRecognition();
        System.out.println("<<<<<Talking Done...");
        List<String> words = new ArrayList<>();
        List<WordResult> results = result.getWords();
        System.out.println(result.getHypothesis());
        for (WordResult wordResult : results ){
            words.add( wordResult.getWord().getSpelling() );
        }
        return words ;
    }
}
