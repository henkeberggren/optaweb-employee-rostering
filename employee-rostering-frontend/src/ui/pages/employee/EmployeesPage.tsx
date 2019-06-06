/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import * as React from 'react';
import { DataTable, DataTableProps, PropertySetter } from 'ui/components/DataTable';
import MultiTypeaheadSelectInput from 'ui/components/MultiTypeaheadSelectInput'
import { employeeSelectors, employeeOperations } from 'store/employee';
import { contractSelectors } from 'store/contract';
import { skillSelectors } from 'store/skill';
import Employee from 'domain/Employee';
import { AppState } from 'store/types';
import { TextInput, Text, Chip, ChipGroup } from '@patternfly/react-core';
import { connect } from 'react-redux';
import Skill from 'domain/Skill';
import Contract from 'domain/Contract';
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import { Predicate, Sorter, ReadonlyPartial } from "types";
import { stringSorter } from 'util/CommonSorters';
import { stringFilter } from 'util/CommonFilters';

interface StateProps extends DataTableProps<Employee> {
  tenantId: number;
  skillList: Skill[];
  contractList: Contract[];
}

const mapStateToProps = (state: AppState): StateProps => ({
  title: "Spots",
  columnTitles: ["Name", "Contract", "Skill Proficiencies"],
  tableData: employeeSelectors.getEmployeeList(state),
  skillList: skillSelectors.getSkillList(state),
  contractList: contractSelectors.getContractList(state),
  tenantId: state.tenantData.currentTenantId
}); 

export interface DispatchProps {
  addEmployee: typeof employeeOperations.addEmployee;
  updateEmployee: typeof employeeOperations.updateEmployee;
  removeEmployee: typeof employeeOperations.removeEmployee;
}

const mapDispatchToProps: DispatchProps = {
  addEmployee: employeeOperations.addEmployee,
  updateEmployee: employeeOperations.updateEmployee,
  removeEmployee: employeeOperations.removeEmployee
};

export type Props = StateProps & DispatchProps;

export class EmployeesPage extends DataTable<Employee, Props> {
  constructor(props: Props) {
    super(props);
    this.addData = this.addData.bind(this);
    this.updateData = this.updateData.bind(this);
    this.removeData = this.removeData.bind(this);
  }

  displayDataRow(data: Employee): JSX.Element[] {
    return [
      <Text key={0}>{data.name}</Text>,
      <Text key={1}>{data.contract.name}</Text>,
      <ChipGroup key={2}>
        {data.skillProficiencySet.map(skill => (
          <Chip key={skill.name} isReadOnly>
            {skill.name}
          </Chip>
        ))}
      </ChipGroup>
    ];
  }

  getInitialStateForNewRow(): Partial<Employee> {
    return {
      skillProficiencySet: []
    };
  }
  
  editDataRow(data: ReadonlyPartial<Employee>, setProperty: PropertySetter<Employee>): JSX.Element[] {
    return [
      <TextInput
        key={0}
        name="name"
        defaultValue={data.name}
        aria-label="Name"
        onChange={(value) => setProperty("name", value)}
      />,
      <TypeaheadSelectInput
        key={1}
        emptyText="Select contract"
        options={this.props.contractList}
        optionToStringMap={contract => contract.name}
        defaultValue={data.contract}
        onChange={contract => setProperty("contract", contract)}
      />,
      <MultiTypeaheadSelectInput
        key={2}
        emptyText="Select skill proficiencies"
        options={this.props.skillList}
        optionToStringMap={skill => skill.name}
        defaultValue={data.skillProficiencySet? data.skillProficiencySet : []}
        onChange={selected => setProperty("skillProficiencySet", selected)}
      />
    ];
  }
  
  isDataComplete(editedValue: ReadonlyPartial<Employee>): editedValue is Employee {
    return editedValue.name !== undefined &&
      editedValue.contract !== undefined &&
      editedValue.skillProficiencySet !== undefined;
  }

  isValid(editedValue: Employee): boolean {
    return editedValue.name.trim().length > 0;
  }

  getFilter(): (filter: string) => Predicate<Employee> {
    return stringFilter(employee => employee.name,
      employee => employee.contract.name,
      employee => employee.skillProficiencySet.map(skill => skill.name));
  }

  getSorters(): (Sorter<Employee> | null)[] {
    return [stringSorter(e => e.name), stringSorter(e => e.contract.name), null];
  }
  
  updateData(data: Employee): void {
    this.props.updateEmployee(data);
  }
  
  addData(data: Employee): void {
    this.props.addEmployee({...data, tenantId: this.props.tenantId});
  }

  removeData(data: Employee): void {
    this.props.removeEmployee(data);
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(EmployeesPage);