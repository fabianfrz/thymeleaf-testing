/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2012, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.thymeleaf.testing.templateengine.standard.testbuilder;

import java.util.HashMap;
import java.util.Map;

import org.thymeleaf.context.IContext;
import org.thymeleaf.fragment.IFragmentSpec;
import org.thymeleaf.testing.templateengine.exception.TestEngineExecutionException;
import org.thymeleaf.testing.templateengine.resource.ITestResource;
import org.thymeleaf.testing.templateengine.standard.directive.StandardTestDirectiveSpec;
import org.thymeleaf.testing.templateengine.standard.directive.StandardTestDirectiveUtils;
import org.thymeleaf.testing.templateengine.testable.FailExpectedTest;
import org.thymeleaf.testing.templateengine.testable.ITest;
import org.thymeleaf.testing.templateengine.testable.SuccessExpectedTest;
import org.thymeleaf.util.Validate;





public class StandardTestBuilder implements IStandardTestBuilder {

    
    public StandardTestBuilder() {
        super();
    }
    

    
    @SuppressWarnings("unchecked")
    public final ITest buildTest(final String executionId, 
            final String documentName, final Map<String,Map<String,Object>> dataByDirectiveAndQualifier) {
        
        Validate.notNull(executionId, "Execution ID cannot be null");
        Validate.notNull(dataByDirectiveAndQualifier, "Data cannot be null");
        
        // Retrieve and process the map of inputs 
        final Map<String,ITestResource> allInputs =
                new HashMap<String, ITestResource>(
                        (Map<String,ITestResource>)(Map<?,?>) dataByDirectiveAndQualifier.get(StandardTestDirectiveSpec.INPUT_DIRECTIVE_SPEC.getName()));
        final ITestResource input = allInputs.get(null);
        allInputs.remove(null);
        
        
        // cache, context, and template mode are required, cannot be null at this point 
        final Boolean cache = (Boolean) getMainDirectiveValue(dataByDirectiveAndQualifier,StandardTestDirectiveSpec.CACHE_DIRECTIVE_SPEC);
        final IContext ctx = (IContext) getMainDirectiveValue(dataByDirectiveAndQualifier,StandardTestDirectiveSpec.CONTEXT_DIRECTIVE_SPEC);
        final String templateMode = (String) getMainDirectiveValue(dataByDirectiveAndQualifier,StandardTestDirectiveSpec.TEMPLATE_MODE_DIRECTIVE_SPEC);
        
        // name and fragmentspec are optional, might be null at this point
        final String name = (String) getMainDirectiveValue(dataByDirectiveAndQualifier,StandardTestDirectiveSpec.TEST_NAME_DIRECTIVE_SPEC);
        final IFragmentSpec fragmentSpec = (IFragmentSpec) getMainDirectiveValue(dataByDirectiveAndQualifier,StandardTestDirectiveSpec.FRAGMENT_DIRECTIVE_SPEC);

        // The presence of output or exception will determine whether this is a success- or a fail-expected test
        final ITestResource output = (ITestResource) getMainDirectiveValue(dataByDirectiveAndQualifier,StandardTestDirectiveSpec.OUTPUT_DIRECTIVE_SPEC); 
        final Class<? extends Throwable> exception = (Class<? extends Throwable>) getMainDirectiveValue(dataByDirectiveAndQualifier,StandardTestDirectiveSpec.EXCEPTION_DIRECTIVE_SPEC);
        
        if (output == null && exception == null) {
            throw new TestEngineExecutionException(
                    executionId, "Neither output nor exception have been specified for test in document " +
                    		     "\"" + documentName + "\". At least one of these must be specified.");
        }
        

        if (output != null) {
            /*
             *  There is an expected output, so this test expects a success
             */
            
            final SuccessExpectedTest test =  new SuccessExpectedTest();
            test.setInput(input);
            test.setAdditionalInputs(allInputs);
            test.setInputCacheable(cache.booleanValue());
            test.setOutput(output);
            test.setName(name);
            test.setTemplateMode(templateMode);
            test.setContext(ctx);
            test.setFragmentSpec(fragmentSpec);
            
            return test;
            
        }

        /*
         * There is no expected output, so this test expects an exception
         */
        
        final String exceptionMessagePattern = (String) getMainDirectiveValue(dataByDirectiveAndQualifier,StandardTestDirectiveSpec.EXCEPTION_MESSAGE_PATTERN_DIRECTIVE_SPEC);

        final FailExpectedTest test = new FailExpectedTest();
        test.setInput(input);
        test.setAdditionalInputs(allInputs);
        test.setInputCacheable(cache.booleanValue());
        test.setOutputThrowableClass(exception);
        test.setOutputThrowableMessagePattern(exceptionMessagePattern);
        test.setName(name);
        test.setTemplateMode(templateMode);
        test.setContext(ctx);
        test.setFragmentSpec(fragmentSpec);

        return test;
        
    }
    
    
    
    
    private static Object getMainDirectiveValue(final Map<String,Map<String,Object>> values, final StandardTestDirectiveSpec<?> directiveSpec) {
        
        final Map<String,Object> directiveValuesByQualifier = values.get(directiveSpec.getName());
        if (directiveValuesByQualifier == null) {
            return null;
        }
        return directiveValuesByQualifier.get(StandardTestDirectiveUtils.MAIN_DIRECTIVE_QUALIFIER);
    }
    
    
    
}
