package edu.unc.lib.bag.validate;

import edu.unc.lib.workers.AbstractBagJob;

/**
 * Asserts that the destination container exists and is the right sort of container.
 * @author count0
 *
 */
public class ValidateDepositContainer extends AbstractBagJob {

	public ValidateDepositContainer(String uuid, String bagDirectory, String depositId) {
		super(uuid, bagDirectory, depositId);
	}

	public ValidateDepositContainer() {
	}

	@Override
	public void run() {

	}

}